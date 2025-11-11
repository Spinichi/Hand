// file: com/hand/hand/ui/model/GroupCodeRepository.kt
package com.hand.hand.ui.model

object GroupCodeRepository {
    // 더미 허용 코드들
    private val validCodes = setOf(
        "123456",
        "654321",
        "ABCDEF"
    )

    fun verify(code: String): Boolean = validCodes.contains(code.trim())
}
