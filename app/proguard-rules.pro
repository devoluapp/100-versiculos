# Gson usa reflection para casar os campos das data classes com as chaves do JSON.
# Sem essas regras, o R8 renomeia os campos em builds de release (minify) e o parse
# falha silenciosamente (exceção engolida em ContentRepositoryImpl.seedInitialData),
# deixando toda categoria sem versículos.
-keepattributes Signature
-keepattributes *Annotation*
-keep class blog.robertotavares.cemversiculos.data.repository.ContentRepositoryImpl$CategoryJson { *; }
-keep class blog.robertotavares.cemversiculos.data.repository.ContentRepositoryImpl$VerseJson { *; }
