package blog.robertotavares.cemversiculos.core.utils

import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.crashlytics.FirebaseCrashlytics

private const val TAG = "TimePickerUtils"

/**
 * android.app.TimePickerDialog, dependendo do fabricante/versão do Android, pode renderizar
 * no modo "spinner" legado (TimePickerSpinnerDelegate), que tem um bug conhecido do próprio
 * framework (NullPointerException/NumberFormatException ao trocar hora/minuto) sem correção
 * possível pelo lado do app - é um erro aberto no AOSP, não algo causado por este código.
 * Como isso derrubava o app (2º erro mais frequente no Crashlytics, provavelmente na tela de
 * horário da notificação), isolamos a abertura do diálogo num try/catch: se acontecer, o app
 * continua de pé, o caso fica registrado no Crashlytics para acompanharmos o impacto, e o
 * usuário só vê um aviso para tentar de novo em vez do app fechando.
 */
fun showSafeTimePickerDialog(
    context: Context,
    initialHour: Int,
    initialMinute: Int,
    onTimeSet: (hour: Int, minute: Int) -> Unit
) {
    try {
        TimePickerDialog(
            context,
            { _, h, m -> onTimeSet(h, m) },
            initialHour,
            initialMinute,
            true
        ).show()
    } catch (e: Exception) {
        Log.w(TAG, "Falha ao abrir o seletor de horário", e)
        FirebaseCrashlytics.getInstance().recordException(e)
        Toast.makeText(
            context,
            "Não foi possível abrir o seletor de horário. Tente novamente.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
