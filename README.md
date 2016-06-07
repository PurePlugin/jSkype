# jSkype
jSkype creation started when skype4web was released, however at the time I was making a private Skype client in Java, not an API. Samczsun, better known as super salter 9000 was creating an extremely limited api at the time of my client creation and still is today. In order to spare people from his limited api, I'm releasing jSkype. 

#Features (confirmed)
- Ping chats with images
- Get contact requests
- Get recent groups
- Get contacts
- Add and remove users from groups
- Send messages
- Format messages
- Accept contact requests
- Send contact requests
- Change a groups topic
- User information
- Search Skype's DB
- Change your profile picture
- Set your online status
- Get info about yourself
- Always online (Doesn't break after 2 days plus can survive internet shortage)
- Create and join groups
- Promote and demote users
 
#Events
- Edit message (UserChatEvent#isEdited)
- TopicChangedEvent (Cancellable)
- UserChatEvent
- UserImagePingEvent
- UserOtherFilesPingEvent
- UserJoinEvent
- UserLeaveEvent
- UserPendingContactRequestEvent
- UserTypingEvent
- ChatPictureChangedEvent
- UserNewMovieAdsPingEvent
- UserRoleChangedEvent
- UserStatusChangedEvent (online status)
- APILoadedEvent

#Downloads, Javadocs, etc
JavaDocs: http://gghost.xyz/JavaDocs/jSkype

Maven: http://maven.gghost.xyz OR http://ghosted.me/maven

Repository:
```
 <repository>
  <id>xyz.gghost</id>
  <url>http://gghost.xyz/maven/</url>
</repository>
```
Dependency:
```
<dependency>
  <groupId>xyz.gghost</groupId>
  <artifactId>jskype</artifactId>
  <version>3.8.1</version>
  <scope>compile</scope>
</dependency>
```

#Creating a skype instance
Before creating a Skype instance, you'll need to confirm whether or not you login with an email/pass or user/pass. If you login with a username and password, you can create a new instance of SkypeAPI with the arguments (username, password).

Example user/pass: 
```java
SkypeAPI skype = new SkypeAPI("NotGhostBot", "Password").login();
```

#Sending chat messages
Sending a message to all contacts example:
```java
```
Sending a message to all recent groups and contacts example:
```java
skype.getGroups().forEach(group -> group.sendMessage("Hi"));
```
Editing a message:
```java
Message message = group.sendMessage(skype, "Hi");
message.editMessage("");
```
## Formatting messages

MessageBuilder is the builder class for constructing string that is safe to pass to Group#sendMessage. In order to add text to the message builder, use #addText. Only use #addHtml with past outputs from #build and html code you know is safe. If you'd like to add two message builders together, simply builderA.build() + builderB.build() would work, however I recommend you to pass the old build output to the constructor of the new builder instance, if you want to make a clean message builder. Otherwise, you can use the FormatUtils class for small, quick jobs.

#Example command handler usage:

```java

public class TestCommand extends Command
{
	public TestCommand()
	{
		super("test");
	}
	
	@Override
	public void execute()
	{
		getChat().sendMessage(getSender().getUsername() + " said Hi!");
	}
}

public class Main
{
	public static void main(String[] args) throws Exception
	{
		SkypeAPI skype = new SkypeAPI("username", "password").login();
		
		skype.getCommandBus().register(new TestCommand());
	}
}

```

#Example event handler usage:

```java
skype.getEventBus().register(UserJoinEvent.class, event ->
{
    System.out.println(event.getUser().getDisplayName() + " has joined " + event.getGroup().getChatId());
});
```

#TODO
- Handle calls (Windows only + semi compatible with wine)

#Dependencies
- commons-lang 3
- org.json (repo contains fork)
- jsoup 
- lombok

#LICENSE
See LICENSE file 
