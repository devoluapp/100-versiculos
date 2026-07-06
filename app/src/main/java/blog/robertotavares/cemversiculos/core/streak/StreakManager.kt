package blog.robertotavares.cemversiculos.core.streak

import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Controla o streak de dias consecutivos de uso: incrementa quando a abertura ocorre
 * no dia seguinte ao último registro e reinicia a contagem quando há uma lacuna.
 */
@Singleton
class StreakManager @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    /** Deve ser chamado uma vez por processo (ex.: Application.onCreate). */
    fun recordAppOpened(): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = dateFormat.format(Date())
        val lastDate = settingsRepository.getLastStreakDate()

        if (lastDate == today) {
            return settingsRepository.getStreakDays()
        }

        val yesterday = dateFormat.format(
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
        )

        val newStreak = if (lastDate == yesterday) settingsRepository.getStreakDays() + 1 else 1

        settingsRepository.saveStreakDays(newStreak)
        settingsRepository.saveLastStreakDate(today)
        return newStreak
    }
}
