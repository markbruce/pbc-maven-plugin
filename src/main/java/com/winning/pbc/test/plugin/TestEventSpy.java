package com.winning.pbc.test.plugin;

import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.eventspy.EventSpy;
import org.codehaus.plexus.component.annotations.Component;

import javax.inject.Named;
import javax.inject.Singleton;


@Component(role = EventSpy.class)
public class TestEventSpy extends AbstractEventSpy {

    Context context ;

    public TestEventSpy(){
        System.out.println("Winning Pbc Maven Plugin Event Spy");
    }
    @Override
    public void init(Context context) throws Exception {
        super.init(context);
        this.context = context;
        System.out.println("spy init");
    }

    @Override
    public void onEvent(Object event) throws Exception {
        super.onEvent(event);
    }

    @Override
    public void close() throws Exception {
        super.close();
        System.out.println("spy close");
    }
}
