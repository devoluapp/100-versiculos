# Relatórios das fontes primárias

Gerados pela análise semanal (Cowork, sábados 8h) a partir do Play Console, Firebase Crashlytics e Google Analytics.
Use estes arquivos como contexto no Claude Code (ex.: "leia relatorios/crashlytics/ e corrija a falha mais frequente").

- `crashlytics/AAAA-MM-DD-crashlytics.md`: problemas abertos com números, stack traces e breadcrumbs.
- `play-console/AAAA-MM-DD-play-console.md`: aquisição, página da loja, avaliações, vitals.
- `analytics/AAAA-MM-DD-eventos.csv`: eventos dos últimos 28 dias (contagem e usuários).
- `snapshots/snapshot-AAAA-MM-DD.json`: todos os números da semana, para comparação.
- `semanal/AAAA-MM-DD-analise.md`: relatório semanal com as ações priorizadas.
- `LATEST-crashlytics.md`: cópia do relatório de Crashlytics mais recente.
