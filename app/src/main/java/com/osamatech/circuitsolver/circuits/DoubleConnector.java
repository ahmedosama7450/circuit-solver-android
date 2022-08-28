package com.osamatech.circuitsolver.circuits;

public abstract class DoubleConnector {

    protected Connector startConnector;
    protected Connector endConnector;

    public Connector getStartConnector() {
        return startConnector;
    }

    public void setStartConnector(Connector startConnector) {
        this.startConnector = startConnector;
    }

    public Connector getEndConnector() {
        return endConnector;
    }

    public void setEndConnector(Connector endConnector) {
        this.endConnector = endConnector;
    }
}
