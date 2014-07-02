/**
 * icplatform Project at Loedige.
 * Closed Source. Not for licence.
 */
package com.lodige.consoleui;

import github.javaappplatform.commons.collection.IObservableCollection;
import github.javaappplatform.commons.events.Event;
import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.platform.PlatformException;
import github.javaappplatform.platform.boot.IBootEntry;
import github.javaappplatform.platform.console.RunCommand;
import github.javaappplatform.platform.extension.Extension;
import github.javaappplatform.platform.extension.ExtensionRegistry;
import github.javaappplatform.platform.job.IJob;
import github.javaappplatform.platform.job.JobPlatform;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;

import com.lodige.logging.LivelogAppender;
import com.lodige.logging.ObservableFixedSizeOrderedSet;

/**
 * TODO javadoc
 * @author renken
 */
public class AWTConsole extends JFrame implements IBootEntry, WindowStateListener, IListener, IJob
{

	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
	
	
	private final MessageConsole logConsole;
	private final MessageConsole consoleConsole;
	private final JTextField cmdPrompt = new JTextField();
	private volatile boolean isClosed;


	/**
	 * @throws HeadlessException
	 */
	public AWTConsole() throws HeadlessException
	{
		super("Console");
		
		this.setSize(800, 800);
		this.setResizable(false);

		Container pane = getContentPane();

		JSplitPane panel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		panel.setResizeWeight(0.5);
		JTextArea log = new JTextArea();
		log.setEditable(false);
		log.setFont(Font.getFont(Font.MONOSPACED));

		panel.add(log);
		JTextArea console = new JTextArea();
		console.setEditable(false);
		console.setFont(Font.getFont(Font.MONOSPACED));
		panel.add(console);
		
		pane.setLayout(new BorderLayout(10, 10));
		pane.add(panel, BorderLayout.CENTER);
		pane.add(this.cmdPrompt, BorderLayout.SOUTH);

		this.logConsole = new MessageConsole(log);
		this.logConsole.setMessageLines(20);
		this.consoleConsole = new MessageConsole(console);
		this.consoleConsole.setMessageLines(20);
		
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        this.init();
        this.setVisible(true);
	}

	
	private final void init()
	{
		final ObservableFixedSizeOrderedSet<ILoggingEvent> eventSet = ExtensionRegistry.getService(LivelogAppender.EXTPOINT_LIVELOG);
		eventSet.addListener(IObservableCollection.EVENT_NEW_ELEMENT, this);
		
		this.cmdPrompt.addActionListener((e) ->
		{
			String cmd = this.cmdPrompt.getText();
			this.cmdPrompt.setText("");
			RunCommand.from(cmd).on(this.consoleConsole.getStreamHandle());
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void windowStateChanged(WindowEvent e)
	{
		if (e.getID() == WindowEvent.WINDOW_CLOSING)
		{
			final ObservableFixedSizeOrderedSet<ILoggingEvent> eventSet = ExtensionRegistry.getService(LivelogAppender.EXTPOINT_LIVELOG);
			eventSet.removeListener(AWTConsole.this);
			AWTConsole.this.isClosed = true;
		}
	}

	
	private void printThrowable(IThrowableProxy proxy, boolean first)
	{
		if (proxy == null)
			return;
		if (!first)
			this.logConsole.writeLine("Caused By:"); //$NON-NLS-1$
		
		this.logConsole.writeLine(proxy.getClassName() + " - " + proxy.getMessage()); //$NON-NLS-1$
		for (StackTraceElementProxy trace : proxy.getStackTraceElementProxyArray())
		{
			this.logConsole.writeLine(trace.getSTEAsString());
		}
		this.printThrowable(proxy.getCause(), false);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleEvent(Event event)
	{
		ILoggingEvent e = (ILoggingEvent) event.getData();
		this.logConsole.writeLine(formatTime(e.getTimeStamp())+" ["+e.getThreadName()+"] " + e.getLoggerName() + " - " + e.getFormattedMessage());
		this.printThrowable(e.getThrowableProxy(), true);
	}

	private static final String formatTime(long millis)
	{
		return TIME_FORMATTER.format(Instant.ofEpochMilli(millis));
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startup(Extension e) throws PlatformException
	{
		JobPlatform.registerJob(this);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown()
	{
		this.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String name()
	{
		return "AWT Console";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long length()
	{
		return IJob.LENGTH_UNKNOWN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long absoluteProgress()
	{
		return IJob.PROGRESS_UNKNOWN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isfinished()
	{
		return this.isClosed;
	}

	private static final long serialVersionUID = -7353598156264204512L;

}
