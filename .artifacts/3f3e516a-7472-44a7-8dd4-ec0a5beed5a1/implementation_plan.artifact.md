# Implementation Plan - Fix Wallet Screen Issues

## Goal Description
Fix three issues on the Wallet screen:
1. Infinite loading if wallet is null.
2. Unresponsive buttons (Deposit, Withdraw, Freeze, Release).
3. Layout & styling improvements.

## Proposed Changes
### Wallet ViewModel & Repository / UI
- **WalletViewModel.kt**: Handle null wallet initialization, add transaction/action methods.
- **WalletScreen.kt**: Fix layout, add dialogs, remove duplicate header, apply M3 styling.

## Verification Plan
- Run `./gradlew :app:assembleDebug`.
