# Cellpinda Mobile Wallet - Android

This project is made to provide Android Wallet to Cellpinda Coin (ERC-20 Token) users.

Supports Ethereum, and any ERC-20 (with a little modification of source code) compatible.

Uses [Infura](https://infura.io/)/[Etherscan](https://etherscan.io/apis)/[Coinmarketcap](https://coinmarketcap.com/api/) APIs, 

and requires additional API services to authenticate to centralized membership application server and to support bonus and point functionality. But it's not a kind of important function in this wallet.

## Built with
* Android Studio
* Kotlin
* Firebase Messaging & Firestore
* Retrofit2
* [Pinlockview](https://github.com/aritraroy/PinLockView)
* [Sweet Alert Dialog](https://github.com/pedant/sweet-alert-dialog)
* Zxing and Glide

## For Test

If you just want to simply run it,
Build this on Android Studio and run, then log-in (Restore wallet) with Test ID / Password (7777 / 7777)

## Current version Features

* Balance inquiry of Ethereum & ERC-20 Token
* Transaction history inquiry of Ethereum & ERC-20 Token

## Features to be implemented

* ETH/ERC-20 transfer function via Web3J, Infura API (sendRawTransaction).

Please refer to Client side [Trust Wallet](https://github.com/TrustWallet/trust-wallet-android-source),
and server side [sendRawTransaction API](https://infura.io/docs/ethereum/json-rpc/eth_sendRawTransaction) at Infura.io.

[<img src="https://wallet.cellpinda.com/img/mobile-wallet-screenshot.jpg">](https://play.google.com/store/apps/details?id=com.cellpinda.coin.wallet)

If any suggestions, contact me to https://github.com/method76
