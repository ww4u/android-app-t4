package com.megarobo.control.event;

import com.megarobo.control.bean.Parameter;

public class ParameterReceiveEvent {

    private Parameter parameter;

    public ParameterReceiveEvent(Parameter parameter){
        this.parameter = parameter;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }
}
