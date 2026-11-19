package com.ieamericanenglish.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalAppDarkMode = compositionLocalOf { false }

@Composable
fun appColor(light: Color): Color {
    if (!LocalAppDarkMode.current) return light
    return when (light) {
        Color.White -> Color(0xFF1E293B)
        Color(0xFFF4F7FC), Color(0xFFF7F9FC), Color(0xFFF8FAFC) -> Color(0xFF0B1220)
        Color(0xFF18243A), Color(0xFF14213D), Color(0xFF1F2937) -> Color(0xFFE7EDF7)
        Color(0xFF718096), Color(0xFF64748B), Color(0xFF4A5568) -> Color(0xFFB5C0D2)
        Color(0xFFE1E7F0), Color(0xFFE5EAF2), Color(0xFFE8EEF6), Color(0xFFE1E5EB) -> Color(0xFF334155)
        Color(0xFF2563EB) -> Color(0xFF7CABFF)
        Color(0xFF0F9D91), Color(0xFF0F8B66) -> Color(0xFF5EDBCB)
        Color(0xFF8B5CF6) -> Color(0xFFB69AFF)
        Color(0xFFB7791F) -> Color(0xFFF2C36B)
        Color(0xFFDB6B35) -> Color(0xFFFFA477)
        Color(0xFFBE4778) -> Color(0xFFFF8DB6)
        Color(0xFFB3BDCC) -> Color(0xFF64748B)
        Color(0xFFE2F5ED) -> Color(0xFF153C35)
        Color(0xFFFFE8E8) -> Color(0xFF4A2630)
        Color(0xFFFFE8E8) -> Color(0xFF4A2630)
        else -> light
    }
}
