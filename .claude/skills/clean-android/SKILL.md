---
name: clean-android
description: Limpa caches e artefatos de build do projeto 100-versiculos (Android/Gradle) que o Android Studio pode não perceber que mudaram. Use SEMPRE depois de editar arquivos de build (build.gradle.kts, proguard-rules.pro, AndroidManifest, versiculo_widget_info.xml), adicionar/remover/renomear recursos em res/, ou antes de dizer ao usuário para abrir o Android Studio e rodar/instalar no dispositivo. Também use se o build falhar com erros de "resource name validation", "duplicate class", classes/recursos "não encontrados" que deveriam existir, ou qualquer erro que pareça inconsistente com o código atual.
---

# Limpeza de build Android (100-versiculos)

## Por que isso existe

Este projeto já teve um build quebrado por um arquivo fantasma
(`ic_bible 2.xml`) dentro de `app/build/intermediates/`, criado por
sincronização de nuvem (iCloud Desktop & Documents) duplicando arquivos do
Gradle em pleno build. O projeto foi movido para `~/Developer/100-versiculos`
(fora do escopo do iCloud) para evitar isso, mas a lição vale em geral:
**o Android Studio e o Gradle mantêm caches incrementais que podem ficar
dessincronizados dos arquivos-fonte quando algo externo ao fluxo normal do
Gradle mexe em `app/build/` ou `.gradle/`** — outro sync de nuvem, um editor
externo, um `git checkout` de branch, etc. Quando isso acontece, o sintoma
típico é um erro de build que não faz sentido olhando o código atual.

Rode esta limpeza proativamente depois de mudanças que o Android Studio não
vai re-observar sozinho (edição de arquivos fora do fluxo normal de save do
IDE, ou quando outra ferramenta/processo mexeu no projeto), e sempre que for
pedir para o usuário instalar/rodar via Android Studio depois de uma sessão
de mudanças.

## JDK

Não há `java` no PATH deste ambiente. Use o JDK embutido do Android Studio:

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

Prefixe todo comando `./gradlew` com isso.

## Limpeza padrão (rotina, rápida)

Rodar depois de qualquer leva de edições, antes de devolver a vez ao usuário
para buildar pelo Android Studio:

```bash
cd ~/Developer/100-versiculos
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew clean
```

Isso apaga `app/build/` (todos os intermediários e artefatos), forçando o
Gradle a reprocessar tudo do zero na próxima build — elimina qualquer
resíduo incremental stale.

## Limpeza profunda (quando o erro persistir ou parecer "impossível")

Se `gradlew clean` + rebuild não resolver, ou o erro citar um arquivo/classe
que não existe no código-fonte atual:

1. Procure por cópias de conflito de sincronização em qualquer lugar do
   repo (padrão clássico de iCloud/Dropbox: `nome 2.ext`, `nome 3.ext`):

   ```bash
   cd ~/Developer/100-versiculos
   find . -path ./.git -prune -o -regex '.* [0-9]\.[a-zA-Z0-9]*$' -print
   ```

   Se aparecer algo fora de `app/build/` ou `.gradle/` (ex.: dentro de
   `app/src/`), é sério — pare e avise o usuário antes de apagar, pode ser
   um arquivo de verdade com nome coincidente. Dentro de `app/build/` ou
   `.gradle/` é sempre seguro remover (são artefatos gerados).

2. Apague o cache Gradle do projeto (não o cache global `~/.gradle`, só o
   local):

   ```bash
   rm -rf ~/Developer/100-versiculos/.gradle
   ```

3. Rode `clean` de novo e depois um build de verificação:

   ```bash
   JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew clean :app:assembleDebug
   ```

4. Se AINDA falhar, o problema é do lado do IDE (índices/caches do Android
   Studio, que não são acessíveis via linha de comando). Diga ao usuário
   para rodar, dentro do Android Studio: **File → Invalidate Caches /
   Restart**.

## Não faça

- Não apague `.idea/` via linha de comando (remove configurações do projeto
  no IDE — run configurations, VCS, etc. — sem necessidade real).
- Não apague `local.properties` (tem o caminho do SDK; regenerar dá
  trabalho ao usuário).
- Não use `git clean -fdx` sem checar `git status` antes — isso apagaria
  qualquer arquivo não commitado, não só cache de build.
