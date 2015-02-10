package org.narrativeandplay.hypedyn.plugins

trait Plugin {
  /**
   * Returns the name of the plugin
   */
  def name: String

  /**
   * Returns the version of the plugin (as per Semantic Versioning 2.0.0 - see http://semver.org/spec/v2.0.0.html)
   */
  def version: String //TODO: change this to a SemVer Version structure
}
