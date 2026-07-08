# Gson usa reflection para casar os campos das data classes com as chaves do JSON.
# Sem essas regras, o R8 renomeia os campos em builds de release (minify) e o parse
# falha silenciosamente (exceção engolida em ContentRepositoryImpl.seedInitialData),
# deixando toda categoria sem versículos.
-keepattributes Signature
-keepattributes *Annotation*
-keep class blog.robertotavares.cemversiculos.data.repository.ContentRepositoryImpl$CategoryJson { *; }
-keep class blog.robertotavares.cemversiculos.data.repository.ContentRepositoryImpl$VerseJson { *; }

# Glance (widgets) instancia ActionCallback via reflection puro (Class.forName + construtor sem
# argumentos), a partir do nome da classe guardado no clique do RemoteViews - ver
# androidx.glance.appwidget.action.RunCallbackAction.run(). Sem manter a classe e o construtor
# vazio, o R8 pode renomear/remover em builds de release, e os botões do widget (ex.: anterior/
# próximo em VersiculoWidgetNavigationAction) param de funcionar silenciosamente: o clique não
# lança exceção visível, só falha a chamar a classe certa.
-keep class * extends androidx.glance.appwidget.action.ActionCallback {
    <init>();
}
