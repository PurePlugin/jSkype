package xyz.gghost.jskype;

import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

public class Logger
{
	@Getter
	@Setter
	private boolean debug;

	public enum Level
	{
		COMMAND,
		INFO,
		ERROR,
		DEBUG;
	}

	public void log(Level level, Object object)
	{
		String date = new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis()));
		String log = String.format("%s | %s | %s", date, level.name(), object);

		switch (level)
		{
		case ERROR:
			System.err.println(log);
			break;

		case INFO:
		default:
			System.out.println(log);
			break;

		case DEBUG:
			if (debug)
				System.out.println(log);
			break;

		}
	}
}