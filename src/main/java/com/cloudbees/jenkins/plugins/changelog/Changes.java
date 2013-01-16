package com.cloudbees.jenkins.plugins.changelog;

import hudson.model.AbstractBuild;
import hudson.model.Api;
import hudson.scm.ChangeLogSet;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
@ExportedBean
public class Changes {

    private List<ChangeLogSet> changes;

    private final AbstractBuild build;

    public Changes(AbstractBuild build, int buildNumber) {
        this.build = build;
        this.changes =  new LinkedList<ChangeLogSet>();
        AbstractBuild b = build;
        while (b != null && b.getNumber() >= buildNumber) {
            changes.add(b.getChangeSet());
            b = (AbstractBuild) b.getPreviousBuild();
        }
    }

    /**
     * Remote API access.
     */
    public final Api getApi() {
        return new Api(this);
    }

    public AbstractBuild getBuild() {
        return build;
    }

    @Exported
    public List<ChangeLogSet> getChanges() {
        return changes;
    }
}
