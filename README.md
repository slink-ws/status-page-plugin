## Atlassian Statuspage Incident Management Plugin for JIRA

This plugin allows management of Atlassian Statuspage incidents directly from Jira.

With this plugin you can:
- link existing incident to Jira issue
- create new incident and assign it to Jira issue
- add/remove affected components to existing incident
- change affected components states
- manage incident lifecycle

## Configuration
For this plugin to work properly some configuration should be performed.

1. In plugin administration (app management section) there should be
 - selected projects for which the plugin should be enabled
 - selected roles to allow plugin management for
 - configured the name of custom field, whilch will be used to store incident-related information (by default _status-page-incident_ is used for field name)

2. In plugin configuration (project configuration) there should be
 - selected roles of users allowed to manage incidents
 - selected roles of users allowed to view incident-related information
 - entered Statuspage API key to access Atlassian Statuspage service

## Usage
Having performed plugin configuration, you'll be able to Link existing Statuspage incidents or Create new incidents.
Links for this actions are accessible from Issue View->More drop down.

## Support
Plugin is provided "as is" and not supported.

## Privacy
Plugin does not collect and store any personal data.
