# Wallet System Implementation Plan

This plan outlines the implementation of a core Wallet and balance/freeze system in the NanaDrop Android app.

## User Review Required

- The wallet will use Room for persistent storage of balance and transactions.
- A new `WalletScreen` will be added to the navigation graph.
- The `MarketplaceDatabase` will be updated to include `WalletEntity` and `WalletTransactionEntity`.

## Proposed Changes

### [Data Layer]

#### [MODIFY] [MarketplaceDatabase.kt](file:///C:/Users/ACER/AndroidStudioProjects/NanaDrop2/app/src/main/java/com/example/nanadrop/data/local/MarketplaceDatabase.kt)
- Update to include `WalletEntity` and `WalletTransactionEntity` in the `@Database` annotation.
- Add `WalletDao`.

#### [NEW] [WalletEntity.kt](file:///C:/Users/ACER/AndroidStudioProjects/NanaDrop2/app/src/main/java/com/example/nanadrop/data/local/WalletEntity.kt)
- Define `WalletEntity` with `availableBalance`, `frozenBalance`, `totalEarned`.

#### [NEW] [WalletTransactionEntity.kt](file:///C:/Users/ACER/AndroidStudioProjects/NanaDrop2/app/src/main/java/com/example/nanadrop/data/local/WalletTransactionEntity.kt)
- Define `WalletTransactionEntity` with fields for transactions.

#### [NEW] [WalletDao.kt](file:///C:/Users/ACER/AndroidStudioProjects/NanaDrop2/app/src/main/java/com/example/nanadrop/data/local/WalletDao.kt)
- Define DAO for `WalletEntity` and `WalletTransactionEntity`.

### [ViewModel/Repository]

#### [NEW] [WalletRepository.kt](file:///C:/Users/ACER/AndroidStudioProjects/NanaDrop2/app/src/main/java/com/example/nanadrop/data/repository/WalletRepository.kt)
- Implement repository methods for wallet operations.

#### [NEW] [WalletViewModel.kt](file:///C:/Users/ACER/AndroidStudioProjects/NanaDrop2/app/src/main/java/com/example/nanadrop/ui/viewmodel/WalletViewModel.kt)
- Implement ViewModel with state management and business logic.

### [UI]

#### [NEW] [WalletScreen.kt](file:///C:/Users/ACER/AndroidStudioProjects/NanaDrop2/app/src/main/java/com/example/nanadrop/ui/screen/WalletScreen.kt)
- Implement Wallet UI.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/ACER/AndroidStudioProjects/NanaDrop2/app/src/main/java/com/example/nanadrop/MainActivity.kt)
- Add "Wallet" route and integration to TopAppBar / navigation.

## Verification Plan

### Automated Tests
- Build the app and verify compilation.

### Manual Verification
- Deploy the app.
- Navigate to the new Wallet screen.
- Verify balances and perform transactions.
