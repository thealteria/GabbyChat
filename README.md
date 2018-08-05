# GabbyChat

GabbyChat is a open-source Android chatting App.

It uses [Firebase's](https://firebase.google.com) Authentication/Database/Storage/Messaging libraries for it's implementation and several other libraries that they are listed on [Third Party Notices](THIRD_PARTY_NOTICES.md).

Based on [Lapit Chat](https://github.com/akshayejh/Lapit---Android-Firebase-Chat-App) [Youtube Series](https://www.youtube.com/playlist?list=PLGCjwl1RrtcQ3o2jmZtwu2wXEA4OIIq53).

App's Privacy Policy and Terms and Conditions will be updated soon.

## Features 

- Messaging
  - Send and Receive messages with users
  - Send pictures
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
MIT License

Copyright (c) 2018 Aman Gupta

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
