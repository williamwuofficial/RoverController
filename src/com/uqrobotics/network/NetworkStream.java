package com.uqrobotics.network;

public interface NetworkStream {
	
	public boolean isStreamEnabled();
	public boolean connect();
	public boolean disconnect();
	public boolean write(String message);

	@Override
	public String toString();
}
