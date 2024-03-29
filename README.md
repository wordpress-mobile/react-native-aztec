# react-native-aztec
## Notice: This repo is no longer maintained. The code from this repo was moved to [the Gutenberg repository](https://github.com/WordPress/gutenberg/tree/trunk/packages/react-native-aztec) and is now maintained there. If you would like to use a Rich Text Editor in your app, please consider using [Gutenberg Mobile](https://github.com/wordpress-mobile/gutenberg-mobile).

Wrapping Aztec Android and Aztec iOS in a React Native component

# License

This work is dual licensed under the [Mozilla Public License version 2.0 (MPL-2.0)](MPL-2.0.md) or the [GNU General Public License v2.0 or later (GPL-2.0)](GPL-2.0.md).

You can choose between one of them, or both if you use this work.

## Android: Run the example app

Make sure to have an emulator running or an Android device connected, and then:

```
$ cd example/
$ yarn clean:install
$ yarn android
```

This will build the Android library (via `gradle`) and example app, then launch the main example activity on your connected device and run the Metro bundler at the same time.

## iOS: Run the example app

Before being able to run the Example App, you'll need to install [Carthage](https://github.com/Carthage/Carthage) and the dependencies for this project:
```
cd ios
carthage bootstrap --platform iOS
```

Then go back to the root directory of the project and do:
```
$ cd example/
$ yarn clean:install
$ yarn ios
```

This will compile the example project, launch metro, run the simulator and run the app.

## FAQ / Troubleshooting

Q: The example app doesn't run

A: Make sure you have yarn and babel installed (https://yarnpkg.com/lang/en/docs/install/)


Q: The example app gets compiled but ReactNative cannot connect to Metro bundler (I'm on a real device attached through USB)

A: To debug on the device through USB, remember to revert ports before launching metro:
`adb reverse tcp:8081 tcp:8081`


Q: The example app gets compiled but ReactNative shows an error

A: try running, from the root folder in the project
```
$ cd example/
$ yarn start --reset-cache
```

Open a new shell window and run either of these depending on the platform:

```
$ yarn android
```

or

```
$ yarn ios
```

