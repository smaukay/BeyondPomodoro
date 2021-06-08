<p align="center">
<img src=https://user-images.githubusercontent.com/84573959/121222540-40114f00-c8a4-11eb-8d29-f6eae2af2511.png alt="app_logo" width="100px" />
</p>

<h2 align="center">BeyondPomodoro</h2>

A minimal Android app for Pomodoro sessions with focus on session notes. 

<p align="center">
<img src=https://user-images.githubusercontent.com/84573959/121213533-ee64c680-c89b-11eb-886c-a673557857d2.png width="200px" />
</p>


# Another Pomodoro app?
A quick search for Pomodoro in play store results in 1000s of apps. We've used several of them. Before this project, our app of choice was [GetFlow](https://github.com/AdrianMiozga/GetFlow/). In a lot of ways, this project is inspited largely by GetFlow.

## But?
The missing piece in the existing Pomodoro apps: notes about each session. The original pomodoro technique forces you to focus on a single task for a set time. This is meant to reduce the cognitive load during the set time. It also forces you to break down your largest objective into a tangible short task.
+ But what about the result of a session?
+ Surely each session is a sucess or a failure or something in between?
+ Maybe the task was too large and so drags on to the next session?

These are questions we felt were important and integral to the Pomodoro technique. These are not addressed in existing apps.

> 
> P.S. [Watson](https://github.com/TailorDev/Watson) is extremely useful for this specific function on the desktop.
>

# Pomodoro + Timetracking = BeyondPomodoro
So was born BeyondPomodoro. To tie notes to Pomodoro sessions in an elegant way. 

<p align="center">
<img src=https://user-images.githubusercontent.com/84573959/121214421-be69f300-c89c-11eb-98e2-2772aa57133b.png width="200px" />
</p>

Instead of adding notes, statistics and analytics into the app, we rely on an existing core app of every phone: the calendar 📅.
We have no intention of reinventing the wheel. 
+ A calendar performs time tracking extremely well
+ There are plenty of calendar apps
+ They come with syncing, searchability, excellent UI, and a variety of themes

## Privacy 
But we also want to focus fundamentally on privacy. So we have no intention of accessing your calendar directly. At the end of each session, you can choose to save the session to your calendar, which opens your calendar app of choice with most of the fields prepopulated. This way, the app stays minimal and non-invasive while also populating the core details in a new calendar event: 

+ start time, 
+ end time, 
+ description based on your tags and activity name

<p align="center">
<img src=https://user-images.githubusercontent.com/84573959/121214581-de011b80-c89c-11eb-954f-d56ac6b4440e.png width="200px" />
</p>

You can choose to simply press save on the calendar event, or edit the details further as you choose. Below is a screen recording of adding notes to a session:

https://user-images.githubusercontent.com/84573959/121221186-f5430780-c8a2-11eb-8128-3f8bf16cdd5b.mp4

# What this app is not 👎
The aim is to be minimal so we decided to steer clear of certain frequently seen features in Pomodoro apps.

## no cloud functionality
Cloud capabilities are required only for backup. Your preferences are already backed up by Android's backup functionality. Apart from that, the app has no other user data, so we have nothing to backup anyway.

## no habit tracking
We believe different people work differently. Some believe in a strict regimen with a specific time for each task. Others work better in a free flowing state. We could never no what works for you. So we steer clear from habit forming functionality. This means we only send notifications at the end of a session. And that's it.

## no app blocking
We don't plan on implementing blacklisting of apps. This feature basically prevents you from using certain "intrusive" apps during your session. But this is a problem because we'd need accessibility permissions and we can read the apps in your system. 
+ we personally wouldn't like a "fancy timer app" to read a list of installed apps. It's far too intrusive
+ ultimately, distractions are a larger issue that an app couldn't solve
+ there are always situations (emergency or otherwise) where you may need to switch apps. We have no way to predict these different use cases

# Future scope
We also wish these features could be included but are not right now

## Do-not-disturb preferences for each activity 📳
This is a common feature which is extremely useful. Unfortunaltely Android 10 no longer allows apps to directly change these settings. An app can only show you the relevant setting and ask you to manually choose. Of course this can get repetitive since it is at the start of each session.

## wifi/network settings 📶
This is again extremely useful but once again we cannot currently: same reason as above.
