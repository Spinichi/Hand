// file: com/hand/hand/ui/admin/member/MemberDetailActivity.kt
package com.hand.hand.ui.admin.member

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MemberDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val orgId = intent.getStringExtra(EXTRA_ORG_ID) ?: "multipampus"
        val memberId = intent.getStringExtra(EXTRA_MEMBER_ID) ?: "1"

        setContent {
            MemberDetailScreen(
                orgId = orgId,
                memberId = memberId,
                onBack = { finish() }
            )
        }
    }

    companion object {
        const val EXTRA_ORG_ID = "extra_org_id"
        const val EXTRA_MEMBER_ID = "extra_member_id"
    }
}
