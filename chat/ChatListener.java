package chat;

import tuplespaces.TupleSpace;

public class ChatListener {
	final String channel_name;
	final TupleSpace ts;
	String next_message_id;
	
	public ChatListener(final TupleSpace ts, final String channel_name) {
		this.ts = ts;
		this.channel_name = channel_name;
		ts.get("connectlock", channel_name);
		String channel_id, num_listeners, newest_message_id;
		final String[] tuple = ts.get("channel", null, channel_name, null, null, null);
		channel_id = tuple[1]; num_listeners = tuple[3]; next_message_id = tuple[4]; newest_message_id = tuple[5];
		for (int i = Integer.parseInt(next_message_id); i < Integer.parseInt(newest_message_id); ++i) {
			String message, times_to_be_read;
			final String[] message_tuple = ts.get("message", channel_name, String.valueOf(i), null, null);
			message = message_tuple[3]; times_to_be_read = message_tuple[4];
			times_to_be_read = String.valueOf(Integer.parseInt(times_to_be_read) + 1);
			ts.put("message", channel_name, String.valueOf(i), message, times_to_be_read);
		}
		num_listeners = String.valueOf(Integer.parseInt(num_listeners) + 1);
		ts.put("channel", channel_id, channel_name, num_listeners, next_message_id, newest_message_id);
		ts.put("connectlock", channel_name);
	}

	public String getNextMessage() {
		String message, times_to_be_read;
		final String[] tuple = ts.get("message", channel_name, next_message_id, null, null);
		message = tuple[3]; times_to_be_read = tuple[4];
		times_to_be_read = String.valueOf(Integer.parseInt(times_to_be_read) - 1);
		ts.put("message", channel_name, next_message_id, message, times_to_be_read);
		next_message_id = String.valueOf(Integer.parseInt(next_message_id) + 1);
		return message;
	}

	public void closeConnection() {
		String channel_id, num_listeners, oldest_message_id, newest_message_id;
		final String[] tuple = ts.get("channel", null, channel_name, null, null, null);
		channel_id = tuple[1]; num_listeners = tuple[3]; oldest_message_id = tuple[4]; newest_message_id = tuple[5];
		for (int i = Integer.parseInt(next_message_id); i < Integer.parseInt(newest_message_id); ++i) {
			String message, times_to_be_read;
			final String[] message_tuple = ts.get("message", channel_name, String.valueOf(i), null, null);
			message = message_tuple[3]; times_to_be_read = message_tuple[4];
			times_to_be_read = String.valueOf(Integer.parseInt(times_to_be_read) - 1);
			ts.put("message", channel_name, String.valueOf(i), message, times_to_be_read);
		}
		num_listeners = String.valueOf(Integer.parseInt(num_listeners) - 1);
		ts.put("channel", channel_id, channel_name, num_listeners, oldest_message_id, newest_message_id);
	}
}
