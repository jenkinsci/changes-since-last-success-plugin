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
import hudson.model.*;
import hudson.model.listeners.RunListener;
import org.kohsuke.stapler.StaplerRequest;

import java.text.ParseException;
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

    public Changes getLastSuccess() {
        Run r = build.getPreviousBuild();
        while (r != null
                && (r.getResult() == null || r.getResult().isWorseThan(Result.UNSTABLE)))
            r = r.getPreviousBuild();
        return new Changes(build, r != null ? r.getNumber() + 1 : build.getNumber());
    }

    public Changes getLastStable() {
        Run r = build.getPreviousBuild();
        while (r != null
                && (r.getResult() == null || r.getResult().isWorseThan(Result.SUCCESS)))
            r = r.getPreviousBuild();
        return new Changes(build, r != null ? r.getNumber() + 1 : build.getNumber());
    }

    public Changes getBuildNumber(String buildNumber) {
        return new Changes(build, Integer.parseInt(buildNumber));
    }

    public Changes getSince(String date) throws ParseException {
        Calendar c = Calendar.getInstance();
        c.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(date));
        Run r = build;
        int buildNumber = Integer.MAX_VALUE;
        while (r != null && r.getTimestamp().after(c)) {
            buildNumber = r.getNumber();
            r = r.getPreviousBuild();
        }
        return new Changes(build, buildNumber);
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
