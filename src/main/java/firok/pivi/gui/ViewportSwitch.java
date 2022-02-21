package firok.pivi.gui;

import java.util.function.Function;

public record ViewportSwitch(ViewportState from, long fromWhen, ViewportState to, long toWhen)
{
	public int calc(Function<ViewportState, Integer> func,
	                long now)
	{
		final int fromValue = func.apply(from);
		final int toValue = func.apply(to);

		if(now <= fromWhen) return fromValue;
		if(now >= toWhen) return toValue;

		final long intervalTotal = toWhen - fromWhen;
		final long intervalNow = now - fromWhen;
		final float intervalPercent = 1f * intervalNow / intervalTotal;
		return (int) ( fromValue + (toValue - fromValue) * intervalPercent );
	}
}
