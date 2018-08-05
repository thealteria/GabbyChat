# GabbyChat

GabbyChat is a open-source Android chatting App.

It uses [Firebase's](https://firebase.google.com) Authentication/Database/Storage/Messaging libraries for it's implementation and several other libraries that they are listed on [Third Party Notices](THIRD_PARTY_NOTICES.md).

App's Privacy Policy and Terms and Conditions will be updated soon.

_Based on [Lapit Chat](https://github.com/akshayejh/Lapit---Android-Firebase-Chat-App) [Youtube Series](https://www.youtube.com/playlist?list=PLGCjwl1RrtcQ3o2jmZtwu2wXEA4OIIq53)._

## Features 

- Messaging
  - Send and Receive messages with users
  - Send pictures
- Lists
  - List with your Requests
  - List with your Messages
  - List with your Friends
  - List with all Users
- Friends
  - Accept, Decline or Remove Friends
- Requests
  - Send or Cancel Friend Request to users
- Profile
  - Update your Profile Picture
  - Update your Status
  - View other users profile
- Notifications
  - Notification when you have a new friend request

**Upcoming**

- Notifications
  - Notification when you have a new message
  - Notification when someone accepts your request
- Blocking
  - Block user from sending messages

## Installation

*Setting up project*

- Download Project
- Create a new [Firebase](https://firebase.google.com) Project in console
- Connect project with Firebase `(Tools/Firebase)` in Android Studio
- Generate, download, paste `google-services.json` into the project

*Setting up notifications back-end*

- Create a folder on your Desktop and open it
- Start CMD (for Windows) or Terminal (for MacOS/Linux)
- Login on Firebase CLI using `firebase login`
- Type `firebase init`, select `Functions` using the `Space` key and hit `Enter`
- Select your App, then `javascript`, `N` on ESLint, and `Y`on dependendcies with npm.
- Navigate `functions` folder and replace `index.js` with [this](note.js)
- Type `firebase deploy` and you are all set

**NOTE:** make sure you read project's [LICENSE](LICENSE.md) before start playing with it.

## License

```
Copyright 2018 Aman.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
