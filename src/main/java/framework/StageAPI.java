package framework;

import java.util.ArrayList;

public interface StageAPI {
    public void Enqueue(Event e);

    public void doJob(ArrayList<Event> elist);
}
