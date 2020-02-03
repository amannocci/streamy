package io.techcode.streamy.util.lang

object Primitives {

  /**
    * Returns size of string representation of an int value.
    *
    * @param value int val.
    * @return size of string representation.
    */
  def stringSize(value: Int): Int = {
    // Initial offset in case of negative number
    var size = 0

    // In negative case we inverse sign
    val tmp = {
      if ((value >> 31) != 0) {
        size += 1
        if (Int.MinValue == value) {
          -(value + 1)
        } else {
          -value
        }
      } else {
        value
      }
    }

    // Divide and conquer
    if (tmp < 100000) {
      if (tmp < 100) {
        if (tmp < 10) {
          size += 1
        } else {
          size += 2
        }
      } else {
        if (tmp < 1000) {
          size += 3
        } else {
          if (tmp < 10000) {
            size += 4
          } else {
            size += 5
          }
        }
      }
    } else {
      if (tmp < 10000000) {
        if (tmp < 1000000) {
          size += 6
        } else {
          size += 7
        }
      } else {
        if (tmp < 100000000) {
          size += 8
        } else {
          if (tmp < 1000000000) {
            size += 9
          } else {
            size += 10
          }
        }
      }
    }
    size
  }

  /**
    * Returns size of string representation of a long value.
    *
    * @param value long val.
    * @return size of string representation.
    */
  def stringSize(value: Long): Int = {
    // Initial offset in case of negative number
    var size = 1

    // In negative case we inverse sign
    var tmp = {
      if ((value >> 63) != 0) {
        size += 1
        if (Long.MinValue == value) {
          -(value + 1)
        } else {
          -value
        }
      } else {
        value
      }
    }

    if (tmp >= 10000000000000000L) {
      size += 16
      tmp /= 10000000000000000L
    }
    if (tmp >= 100000000) {
      size += 8
      tmp /= 100000000
    }
    if (tmp >= 10000) {
      size += 4
      tmp /= 10000
    }
    if (tmp >= 100) {
      size += 2
      tmp /= 100
    }
    if (tmp >= 10) {
      size += 1
    }
    size
  }

}
