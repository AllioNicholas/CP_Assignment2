package chat;

import tuplespaces.TupleSpace;

public class ChatServer {
	final int rows;
	final TupleSpace ts;
	
	@Override
	public String toString() {
		return ts.toString();
	}
	
	public ChatServer(TupleSpace ts, int rows, String[] channelNames) {
		this.ts = ts;
		this.rows = rows;
		ts.put("server", String.valueOf(channelNames.length), String.valueOf(rows));
		for (int i = 0; i < channelNames.length; ++i) {
			ts.put("channel", String.valueOf(i), channelNames[i], "0", "0", "0");
			ts.put("writerlock", channelNames[i]);
			ts.put("connectlock", channelNames[i]);
		}
	}

	public ChatServer(TupleSpace ts) {
		this.ts = ts;
		final String[] tuple = ts.read("server", null, null);
		this.rows = Integer.parseInt(tuple[2]);
	}

	public String[] getChannels() {
		final String[] tuple = ts.read("server", null, null);
		final int num_channels = Integer.parseInt(tuple[1]);
		final String[] channel_names = new String[num_channels];
		for (int i = 0; i < num_channels; ++i) {
			String channel_name;
			final String[] channel_tuple = ts.read("channel", String.valueOf(i), null, null, null, null);
			channel_name = channel_tuple[2];
			channel_names[i] = channel_name;
		}
		return channel_names;
	}

	public void writeMessage(String channel, String message) {
		ts.get("connectlock", channel);
		ts.get("writerlock", channel);
		String channel_id; int num_listeners, oldest_message_id, newest_message_id;
		String[] channel_tuple = ts.get("channel", null, channel, null, null, null);
		channel_id = channel_tuple[1]; num_listeners = Integer.parseInt(channel_tuple[3]); oldest_message_id = Integer.parseInt(channel_tuple[4]); newest_message_id = Integer.parseInt(channel_tuple[5]);
		final int num_messages = newest_message_id - oldest_message_id;
		if (num_messages >= rows) {
			ts.put("channel", channel_id, channel, String.valueOf(num_listeners), String.valueOf(oldest_message_id), String.valueOf(newest_message_id));
			ts.get("message", channel, String.valueOf(oldest_message_id), null, "0");
			channel_tuple = ts.get("channel", null, channel, null, String.valueOf(oldest_message_id), String.valueOf(newest_message_id));
			channel_id = channel_tuple[1]; num_listeners = Integer.parseInt(channel_tuple[3]);
			oldest_message_id += 1;
		}
		ts.put("message", channel, String.valueOf(newest_message_id), message, String.valueOf(num_listeners));
		newest_message_id += 1;
		ts.put("channel", channel_id, channel, String.valueOf(num_listeners), String.valueOf(oldest_message_id), String.valueOf(newest_message_id));
		ts.put("writerlock", channel);
		ts.put("connectlock", channel);
	}

	public ChatListener openConnection(String channel) {
		return new ChatListener(ts, channel);
	}
}
