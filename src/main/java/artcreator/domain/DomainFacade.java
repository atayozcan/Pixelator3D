package artcreator.domain;

import artcreator.domain.impl.DomainImpl;
import artcreator.domain.port.Domain;

public class DomainFacade implements DomainFactory, Domain {
    private DomainImpl domain;

    @Override
    public synchronized Domain domain() {
        if (this.domain != null) return this;
        this.domain = new DomainImpl();
        return this;
    }

    @Override
    public synchronized Template mkTemplate() {
        return this.domain.mkTemplate();
    }
}
