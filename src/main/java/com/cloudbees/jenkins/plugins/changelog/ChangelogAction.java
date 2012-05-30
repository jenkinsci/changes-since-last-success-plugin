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
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.File;
import java.io.IOException;

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

    public Object getDynamic(String buildNumber, StaplerRequest req, StaplerResponse rsp)
            throws Exception {

        // TODO compute changelog between current build and buildNumber
        // TODO handle buildNumber == "lastSuccess"
        return this;
    }

    public String getIconFileName() {
        return "notepad.gif";
    }

    public String getDisplayName() {
        return "Changelog";
    }

    public String getUrlName() {
        return "changelog";
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
