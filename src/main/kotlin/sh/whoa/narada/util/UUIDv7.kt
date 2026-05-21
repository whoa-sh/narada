package sh.whoa.narada.util

import java.security.SecureRandom
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

object UUIDv7 {
	private val numberGenerator: ThreadLocal<SecureRandom> = ThreadLocal.withInitial { SecureRandom() }

	// Packed monotonic state: upper bits = millis, low 12 bits = sequence
	private val lastState = AtomicLong(packState(Long.MIN_VALUE, 0))

	/**
	 * @return A UUID object representing a UUIDv7 value.
	 */
	fun randomUUID(): UUID {
		val bytes = randomBytesMonotonic()
		val msb = bytes.toLongBE(0)
		val lsb = bytes.toLongBE(8)
		return UUID(msb, lsb)
	}

	/**
	 * Generates a 16-byte array.
	 * The first 6 bytes contain the current timestamp in milliseconds.
	 * The next bytes are random, with specific bits set for version and variant.
	 *
	 * Layout:
	 *  - [0..5]   : 48-bit Unix epoch milliseconds (timestamp)
	 *  - [6]      : high nibble = version 7, low nibble = rand_a[11:8]
	 *  - [7]      : rand_a[7:0] (12-bit monotonic sequence, wraps mod 4096)
	 *  - [8]      : IETF variant in high 2 bits, lower 6 bits random
	 *  - [9..15]  : remaining 62 bits of randomness across [8..15], except variant bits
	 *
	 *
	 * @return A ByteArray of 16 bytes representing the UUIDv7.
	 */
	private fun randomBytesMonotonic(): ByteArray {
		val value = ByteArray(16).also { numberGenerator.get().nextBytes(it) }

		val now = System.currentTimeMillis()
		val state = nextState(now)
		val ts = unpackMillis(state)
		val seq = unpackSeq(state)

		value[0] = ((ts ushr 40) and 0xFF).toByte()
		value[1] = ((ts ushr 32) and 0xFF).toByte()
		value[2] = ((ts ushr 24) and 0xFF).toByte()
		value[3] = ((ts ushr 16) and 0xFF).toByte()
		value[4] = ((ts ushr 8) and 0xFF).toByte()
		value[5] = (ts and 0xFF).toByte()

		// Set the version to 7 in high nibble of byte 6
		value[6] = (((0x7 shl 4) or ((seq ushr 8) and 0x0F))).toByte()

		// Set low 8 bits of rand_a in byte 7
		value[7] = (seq and 0xFF).toByte()

		// Set the variant to IETF variant
		value[8] = ((value[8].toInt() and 0x3F) or 0x80).toByte()

		return value
	}

	// Big-endian 8-byte to long
	private fun ByteArray.toLongBE(offset: Int = 0): Long =
		java.nio.ByteBuffer
			.wrap(this)
			.getLong(offset)

	/**
	 * Returns the packed monotonic state (timestamp and sequence) for the given millisecond:
	 * - If [ts] > last seen timestamp, seed with a random 12-bit value.
	 * - If [ts] <= last seen, increment sequence. If sequence overflows 4095, increment timestamp and reset sequence.
	 *
	 * Uses CAS on a packed [lastState] value to keep timestamp+sequence transitions atomic.
	 */
	private fun nextState(ts: Long): Long {
		while (true) {
			val prevState = lastState.get()
			val lastTs = unpackMillis(prevState)
			val lastSeq = unpackSeq(prevState)

			if (ts > lastTs) {
				// New millisecond: randomize the starting point to retain entropy
				val seeded = numberGenerator.get().nextInt(1 shl 12)
				val nextState = packState(ts, seeded)
				if (lastState.compareAndSet(prevState, nextState)) {
					return nextState
				}
			} else {
				// Same millisecond or clock moved backwards: increment sequence
				val nextSeq = lastSeq + 1
				val nextState =
					if (nextSeq > 0x0FFF) {
						// Overflow: increment timestamp and reset sequence to 0
						packState(lastTs + 1, 0)
					} else {
						packState(lastTs, nextSeq)
					}
				if (lastState.compareAndSet(prevState, nextState)) {
					return nextState
				}
			}
			// If either CAS fails, retry with fresh reads
		}
	}

	private fun packState(
		millis: Long,
		seq: Int,
	): Long = (millis shl 12) or (seq.toLong() and 0x0FFFL)

	private fun unpackMillis(state: Long): Long = state shr 12

	private fun unpackSeq(state: Long): Int = (state and 0x0FFFL).toInt()
}
