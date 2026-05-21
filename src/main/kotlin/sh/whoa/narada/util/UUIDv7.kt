package sh.whoa.narada.util

import java.security.SecureRandom
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

object UUIDv7 {
	private val numberGenerator: ThreadLocal<SecureRandom> = ThreadLocal.withInitial { SecureRandom() }

	// Packed monotonic state: upper bits = millis, low 12 bits = sequence
	private val lastState = AtomicLong(0)

	/**
	 * @return A UUID object representing a UUIDv7 value.
	 */
	fun randomUUID(): UUID {
		val now = System.currentTimeMillis()
		val state = nextState(now)
		val ts = unpackMillis(state)
		val seq = unpackSeq(state)

		// MSB: 48 bits timestamp | 4 bits version (7) | 12 bits sequence
		val msb = (ts shl 16) or (0x7L shl 12) or (seq.toLong() and 0x0FFFL)

		// LSB: 2 bits variant (2) | 62 bits randomness
		val random = numberGenerator.get().nextLong()
		val lsb = (random and 0x3FFFFFFFFFFFFFFFL) or (Long.MIN_VALUE)

		return UUID(msb, lsb)
	}

	/**
	 * Returns the packed monotonic state (timestamp and sequence) for the given millisecond.
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
