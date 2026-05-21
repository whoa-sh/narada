package sh.whoa.narada.util

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.util.Collections
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class UUIDv7Test {
	@AfterEach
	fun resetState() {
		val lastStateField = UUIDv7::class.java.getDeclaredField("lastState")
		lastStateField.isAccessible = true
		val lastState = lastStateField.get(null) as java.util.concurrent.atomic.AtomicLong
		lastState.set(0)
	}

	@Test
	fun `test no duplicate UUID should be generated`() {
		val uuidSet = mutableSetOf<UUID>()
		repeat(100_000) {
			val uuid = UUIDv7.randomUUID()
			assertTrue(uuidSet.add(uuid)) { "Duplicate UUID generated: $uuid" }
		}
	}

	@Test
	fun `test version and variant bits are set correctly`() {
		val uuid = UUIDv7.randomUUID()

		// Version should be 7
		val version = uuid.version()
		assertEquals(7, version) { "UUID version is not 7" }

		// Variant should be IETF variant
		val variant = uuid.variant()
		assertEquals(2, variant) { "UUID variant is not IETF variant (2)" }
	}

	@Test
	fun `test timestamp embedded timestamp is in the past`() {
		val uuid = UUIDv7.randomUUID()

		val timestamp = extractTimestamp(uuid)

		val currentTime = System.currentTimeMillis()

		// Allow a small delta due to execution time
		val delta = currentTime - timestamp
		assertTrue(delta in 0..1000) { "Timestamp in UUID is not recent (delta: $delta ms)" }
	}

	@Test
	fun `test UUIDv7 should be ordered`() {
		val uuid1 = UUIDv7.randomUUID()
		Thread.sleep(1)
		val uuid2 = UUIDv7.randomUUID()

		// Since UUIDv7 includes the timestamp, uuid2 should be greater than uuid1
		assertTrue(uuid1 < uuid2) { "UUIDs are not ordered correctly" }
	}

	@Test
	fun `test monotonicity within the same millisecond`() {
		val window = mutableListOf<UUID>()
		var lastTs = -1L

		// Generate until we get a window of >= 5 with identical timestamp
		repeat(50_000) {
			val u = UUIDv7.randomUUID()
			val ts = extractTimestamp(u)
			if (window.isEmpty()) {
				window.add(u)
				lastTs = ts
			} else if (ts == lastTs) {
				window.add(u)
			} else {
				if (window.size >= 5) return@repeat
				window.clear()
				window.add(u)
				lastTs = ts
			}
		}

		assertTrue(window.size >= 5, "Failed to capture enough UUIDs in the same millisecond")

		// rand_a should strictly increase for same-ms sequence until a wrap
		val seqs = window.map { extractRandA(it) }
		for (i in 1 until seqs.size) {
			assertTrue(seqs[i] > seqs[i - 1], "rand_a did not increase within the same millisecond")
		}

		// Lexicographic UUID order should also increase
		for (i in 1 until window.size) {
			assertTrue(window[i - 1] < window[i], "UUIDs not strictly increasing within same ms")
		}
	}

	@Test
	fun `test monotonicity across milliseconds`() {
		val u1 = UUIDv7.randomUUID()

		// busy-wait until the next millisecond to avoid sleep granularity
		val ts1 = extractTimestamp(u1)

		var u2: UUID
		while (true) {
			u2 = UUIDv7.randomUUID()
			if (extractTimestamp(u2) > ts1) break
		}

		assertTrue(u1 < u2, "UUID with later millisecond should be greater")
	}

	/** 12-bit rand_a is split as: byte6 low-nibble = high 4 bits, byte7 = low 8 bits. */
	private fun extractRandA(uuid: UUID): Int {
		val msb = uuid.mostSignificantBits
		// Recreate bytes 6 and 7 from MSB
		// MSB layout (bits): [0..63] is bytes 0..7
		// We need byte6 (bits 16..23 of MSB) and byte7 (bits 24..31 of MSB) if using big-endian packing
		val bytes = ByteArray(16)
		val buf = ByteBuffer.wrap(bytes)
		buf.putLong(msb)
		buf.putLong(uuid.leastSignificantBits)
		val b6 = bytes[6].toInt() and 0xFF
		val b7 = bytes[7].toInt() and 0xFF
		val high4 = b6 and 0x0F
		return (high4 shl 8) or b7
	}

	@Test
	fun `rand_a stays within 12-bit range`() {
		repeat(10_000) {
			val u = UUIDv7.randomUUID()
			val seq = extractRandA(u)
			assertTrue(seq in 0..0x0FFF, "rand_a out of 12-bit range: $seq")
		}
	}

	@Test
	fun `test concurrent generation yields no duplicates and global non-decreasing after sort`() {
		val threads = Runtime.getRuntime().availableProcessors().coerceAtLeast(4)
		val perThread = 12_000 / threads
		val pool = Executors.newFixedThreadPool(threads)

		val start = CountDownLatch(1)
		val done = CountDownLatch(threads)

		val list = Collections.synchronizedList(mutableListOf<UUID>())

		repeat(threads) {
			pool.execute {
				start.await()
				repeat(perThread) { list += UUIDv7.randomUUID() }
				done.countDown()
			}
		}

		// start together
		start.countDown()
		assertTrue(done.await(30, TimeUnit.SECONDS), "Workers did not finish in time")
		pool.shutdownNow()

		// 1) No duplicates
		val set = list.toSet()
		assertEquals(list.size, set.size, "Duplicate UUIDs detected in concurrent generation")

		// 2) Global non-decreasing when sorted by natural UUID order
		val sorted = list.sorted()
		for (i in 1 until sorted.size) {
			assertTrue(sorted[i - 1] <= sorted[i], "Global order regressed at index $i")
		}
	}

	@Test
	fun `test concurrent generation - same-ms groups strictly increase by rand_a and match UUID order`() {
		val threads = Runtime.getRuntime().availableProcessors().coerceAtLeast(4)
		val perThread = 10_000 / threads
		val pool = Executors.newFixedThreadPool(threads)

		val start = CountDownLatch(1)
		val done = CountDownLatch(threads)

		val uuids = Collections.synchronizedList(mutableListOf<UUID>())
		repeat(threads) {
			pool.execute {
				start.await()
				repeat(perThread) { uuids += UUIDv7.randomUUID() }
				done.countDown()
			}
		}

		start.countDown()
		assertTrue(done.await(30, TimeUnit.SECONDS))
		pool.shutdownNow()

		// Group by embedded timestamp
		val byTs: Map<Long, List<UUID>> = uuids.groupBy(::extractTimestamp)

		// For each timestamp:
		//  - rand_a values are unique
		//  - sorting by rand_a equals sorting by UUID (because version nibble is constant and rand_a sits before rand_b)
		//  - UUIDs are strictly increasing when ordered by rand_a
		for ((ts, group) in byTs) {
			if (group.size <= 1) continue

			val withSeq = group.map { it to extractRandA(it) }
			val seqs = withSeq.map { it.second }

			// uniqueness of 12-bit counter in observed set
			assertEquals(seqs.size, seqs.toSet().size, "Duplicate rand_a within ts=$ts")

			// sort by seq and by UUID and compare order
			val bySeq = withSeq.sortedBy { it.second }.map { it.first }
			val byUuid = group.sorted()

			assertEquals(bySeq, byUuid, "Within ts=$ts, UUID order does not match rand_a order")

			// strictly increasing by UUID when ordered by seq
			for (i in 1 until bySeq.size) {
				assertTrue(bySeq[i - 1] < bySeq[i], "Within ts=$ts, UUIDs not strictly increasing")
			}
		}
	}

	@Test
	fun `across milliseconds - later timestamp yields greater UUID`() {
		val u1 = UUIDv7.randomUUID()
		val ts1 = extractTimestamp(u1)
		var u2: UUID
		do {
			u2 = UUIDv7.randomUUID()
		} while (extractTimestamp(u2) == ts1)

		assertTrue(u1 < u2, "UUID with later millisecond should be greater")
	}

	@Test
	fun `test clock regression - should increment sequence but stay monotonic`() {
		val u1 = UUIDv7.randomUUID()
		val ts1 = extractTimestamp(u1)

		// Mock a future state in UUIDv7 using reflection
		val lastStateField = UUIDv7::class.java.getDeclaredField("lastState")
		lastStateField.isAccessible = true
		val lastState = lastStateField.get(null) as java.util.concurrent.atomic.AtomicLong

		// Set state to 1 second in the future with sequence 100
		val futureTs = ts1 + 1000
		val futureSeq = 100
		val futureState = (futureTs shl 12) or (futureSeq.toLong() and 0x0FFF)
		lastState.set(futureState)

		// Generate new UUID - should hit the 'else' branch because currentTime < futureTs
		val u2 = UUIDv7.randomUUID()
		val ts2 = extractTimestamp(u2)
		val seq2 = extractRandA(u2)

		assertEquals(futureTs, ts2, "Should have used the future timestamp from state")
		assertEquals(futureSeq + 1, seq2, "Should have incremented the sequence from state")
		assertTrue(u1 < u2)
	}

	@Test
	fun `test sequence overflow - should increment timestamp and reset sequence`() {
		// Mock state at the very end of a millisecond sequence
		val lastStateField = UUIDv7::class.java.getDeclaredField("lastState")
		lastStateField.isAccessible = true
		val lastState = lastStateField.get(null) as java.util.concurrent.atomic.AtomicLong

		val ts = System.currentTimeMillis()
		val maxSeq = 0x0FFF
		val state = (ts shl 12) or (maxSeq.toLong() and 0x0FFF)
		lastState.set(state)

		// Next ID should overflow sequence and increment timestamp
		val u = UUIDv7.randomUUID()
		val tsAfter = extractTimestamp(u)
		val seqAfter = extractRandA(u)

		assertEquals(ts + 1, tsAfter, "Timestamp should have incremented due to overflow")
		assertEquals(0, seqAfter, "Sequence should have reset to 0")
	}

	private fun extractTimestamp(uuid: UUID): Long {
		val bytes = ByteArray(16)
		val buffer = ByteBuffer.wrap(bytes)
		buffer.putLong(uuid.mostSignificantBits)
		buffer.putLong(uuid.leastSignificantBits)

		val timestampBytes = bytes.sliceArray(0..5)
		var timestamp = 0L
		for (byte in timestampBytes) {
			timestamp = (timestamp shl 8) or (byte.toLong() and 0xFF)
		}
		return timestamp
	}
}
