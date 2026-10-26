# Graph Report - .  (2026-07-08)

## Corpus Check
- Corpus is ~24,639 words - fits in a single context window. You may not need a graph.

## Summary
- 207 nodes · 280 edges · 38 communities (17 shown, 21 thin omitted)
- Extraction: 95% EXTRACTED · 5% INFERRED · 0% AMBIGUOUS · INFERRED: 15 edges (avg confidence: 0.82)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Database & Repo (C0)
- Database & Repo (C1)
- UI Screens (C2)
- Database & Repo (C3)
- Database & Repo (C4)
- UI Screens (C5)
- Database & Repo (C6)
- UI Screens (C7)
- UI Screens (C8)
- CashierViewModelFactory.kt
- gradlew
- ExampleInstrumentedTest.kt
- ExampleRobolectricTest.kt
- ExampleUnitTest.kt
- Transaction
- recordToolUse.sh script
- Warung Kopi Logo
- Launcher Icons (C20)
- Launcher Icons (C21)
- Launcher Icons (C22)
- Launcher Icons (C23)
- Launcher Icons (C24)
- Launcher Icons (C25)
- Launcher Icons (C26)
- Launcher Icons (C27)
- Launcher Icons (C28)
- Launcher Icons (C29)
- UI Screens (C30)
- Category
- Product
- TransactionItem
- UserProfile

## God Nodes (most connected - your core abstractions)
1. `CashierViewModel` - 41 edges
2. `CashierRepository` - 19 edges
3. `Product` - 16 edges
4. `Category` - 9 edges
5. `Transaction` - 8 edges
6. `ProductDao` - 7 edges
7. `TransactionDao` - 7 edges
8. `AppDatabase` - 7 edges
9. `TransactionItem` - 7 edges
10. `UserProfile` - 7 edges

## Surprising Connections (you probably didn't know these)
- `Easy Cashier POS` --conceptually_related_to--> `AppDatabase`  [INFERRED]
  README.md → DOKUMENTASI_KODE.md
- `Easy Cashier POS` --conceptually_related_to--> `MainScreen`  [INFERRED]
  README.md → DOKUMENTASI_KODE.md
- `MainScreen()` --calls--> `CashierScreen()`  [INFERRED]
  app/src/main/java/com/example/ui/screen/MainScreen.kt → app/src/main/java/com/example/ui/screen/CashierScreen.kt
- `MainScreen()` --calls--> `ProductScreen()`  [INFERRED]
  app/src/main/java/com/example/ui/screen/MainScreen.kt → app/src/main/java/com/example/ui/screen/ProductScreen.kt
- `MainScreen()` --calls--> `ReportScreen()`  [INFERRED]
  app/src/main/java/com/example/ui/screen/MainScreen.kt → app/src/main/java/com/example/ui/screen/ReportScreen.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **MVVM Architecture Components** — dokumentasi_kode_mainscreen, dokumentasi_kode_cashierviewmodel, dokumentasi_kode_cashierrepository, dokumentasi_kode_appdatabase [EXTRACTED 1.00]
- **Database Entities** — dokumentasi_kode_category, dokumentasi_kode_product, dokumentasi_kode_transaction, dokumentasi_kode_transactionitem, dokumentasi_kode_userprofile [EXTRACTED 1.00]
- **App Launcher Icons** — app_src_main_res_mipmap_hdpi_ic_launcher_icon, app_src_main_res_mipmap_hdpi_ic_launcher_round_icon, app_src_main_res_mipmap_mdpi_ic_launcher_icon, app_src_main_res_mipmap_mdpi_ic_launcher_round_icon, app_src_main_res_mipmap_xhdpi_ic_launcher_icon, app_src_main_res_mipmap_xhdpi_ic_launcher_round_icon, app_src_main_res_mipmap_xxhdpi_ic_launcher_icon, app_src_main_res_mipmap_xxhdpi_ic_launcher_round_icon, app_src_main_res_mipmap_xxxhdpi_ic_launcher_icon, app_src_main_res_mipmap_xxxhdpi_ic_launcher_round_icon [EXTRACTED 1.00]

## Communities (38 total, 21 thin omitted)

### Community 0 - "Database & Repo (C0)"
Cohesion: 0.11
Nodes (10): CartItemModel, CashierViewModel, Boolean, Double, Int, List, Long, String (+2 more)

### Community 1 - "Database & Repo (C1)"
Cohesion: 0.11
Nodes (12): Flow, List, String, ProductDao, Product, CashierRepository, Double, Flow (+4 more)

### Community 2 - "UI Screens (C2)"
Cohesion: 0.11
Nodes (14): MainActivity, Cashier, MainScreen(), Products, Profile, Reports, Screen, Modifier (+6 more)

### Community 3 - "Database & Repo (C3)"
Cohesion: 0.19
Nodes (7): Flow, Int, List, Long, TransactionDao, Transaction, TransactionItem

### Community 4 - "Database & Repo (C4)"
Cohesion: 0.15
Nodes (7): Flow, UserProfileDao, AppDatabase, getDatabase(), UserProfile, Context, RoomDatabase

### Community 5 - "UI Screens (C5)"
Cohesion: 0.18
Nodes (15): AppDatabase, CashierRepository, CashierScreen, CashierViewModel, CategoryDao, MainScreen, ProductDao, ProductScreen (+7 more)

### Community 6 - "Database & Repo (C6)"
Cohesion: 0.16
Nodes (5): CategoryDao, Flow, List, String, Category

### Community 7 - "UI Screens (C7)"
Cohesion: 0.24
Nodes (9): CartContent(), CashierScreen(), List, Modifier, ReceiptDialog(), Modifier, ReportScreen(), com (+1 more)

### Community 8 - "UI Screens (C8)"
Cohesion: 0.38
Nodes (6): Int, List, Modifier, String, ProductScreen(), SlidingTabs()

### Community 9 - "CashierViewModelFactory.kt"
Cohesion: 0.33
Nodes (4): CashierViewModelFactory, Class, T, ViewModelProvider

### Community 10 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **20 isolated node(s):** `recordToolUse.sh script`, `Category`, `Product`, `Transaction`, `TransactionItem` (+15 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **21 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CashierViewModel` connect `Database & Repo (C0)` to `Database & Repo (C1)`, `UI Screens (C2)`, `Database & Repo (C3)`, `Database & Repo (C4)`, `UI Screens (C7)`, `UI Screens (C8)`, `CashierViewModelFactory.kt`?**
  _High betweenness centrality (0.319) - this node is a cross-community bridge._
- **Why does `MainScreen()` connect `UI Screens (C2)` to `UI Screens (C8)`, `CashierViewModelFactory.kt`, `UI Screens (C7)`?**
  _High betweenness centrality (0.117) - this node is a cross-community bridge._
- **Why does `Product` connect `Database & Repo (C1)` to `Database & Repo (C0)`?**
  _High betweenness centrality (0.105) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `Category` (e.g. with `.addCategory()` and `.deleteCategory()`) actually correct?**
  _`Category` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `recordToolUse.sh script`, `Category`, `Product` to the rest of the system?**
  _22 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Database & Repo (C0)` be split into smaller, more focused modules?**
  _Cohesion score 0.1051693404634581 - nodes in this community are weakly interconnected._
- **Should `Database & Repo (C1)` be split into smaller, more focused modules?**
  _Cohesion score 0.11330049261083744 - nodes in this community are weakly interconnected._