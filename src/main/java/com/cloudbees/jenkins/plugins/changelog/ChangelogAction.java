/*
 * Copyright (C) 2012 CloudBees Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 */

package com.cloudbees.jenkins.plugins.changelog;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import hudson.scm.ChangeLogParser;
import hudson.scm.ChangeLogSet;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author: <a hef="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class ChangelogAction implements Action {

    private final AbstractBuild build;

    public ChangelogAction(AbstractBuild build) {
        this.build = build;
    }

    public AbstractBuild getBuild() {
        return build;
    }

    public int getRangeEndBuildNumber(StaplerRequest req) {
        Integer buildNumber = (Integer) req.getAttribute("buildNumber");
        if (buildNumber != null) return buildNumber.intValue();

        // default to lastStable
        return build.getProject().getLastStableBuild().getNumber();
    }

    public List<ChangeLogSet> getChanges(int buildNumber) {
        List<ChangeLogSet> changes =  new LinkedList<ChangeLogSet>();
        AbstractBuild b = build;
        while (b != null && b.getNumber() >= buildNumber) {
            changes.add(b.getChangeSet());
            b = (AbstractBuild) b.getPreviousBuild();
        }


        return changes;
    }

    /**
     * Handle rendering changelog for an arbitrary range
     */
    public Object getDynamic(String range, StaplerRequest req, StaplerResponse rsp)
            throws Exception {

        int buildNumber;
        if ("lastSuccess".equals(range)) {
            buildNumber = build.getProject().getLastSuccessfulBuild().getNumber() + 1;
        } else if ("lastStable".equals(range)) {
            buildNumber = build.getProject().getLastStableBuild().getNumber() + 1;
        } else if ("since".equals(range)) {
            String date = req.getParameter("date");
            String format = req.hasParameter("format") ? req.getParameter("format") : "ddMMyyyyhhmmss";

            Calendar c = Calendar.getInstance();
            c.setTime(new SimpleDateFormat(format).parse(date));
            req.setAttribute("since", c);
            Run r = build;
            buildNumber = r.getNumber();
            while (r != null && r.getTimestamp().after(c)) {
                buildNumber = r.getNumber();
                r = r.getPreviousBuild();
            }
        }
        else { buildNumber = Integer.parseInt(range); }

        req.setAttribute("range", range);
        req.setAttribute("buildNumber", buildNumber);
        return this;
    }

    public String getIconFileName() {
        return "notepad.gif";
    }

    public String getDisplayName() {
        return "Changes since last success";
    }

    public String getUrlName() {
        return "changes-since-last-success";
    }

    // FIXME no TransientBuildActionFactory to add an action to all builds ?
    @Extension
    public final static RunListener l = new RunListener<AbstractBuild>() {
        @Override
        public void onCompleted(AbstractBuild build, TaskListener listener) {
            build.addAction(new ChangelogAction(build));
        }
    };
}
