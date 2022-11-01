package org.openhab.binding.icloud.internal.json.request;

/**
 * TODO simon This type ...
 *
 */
public class ICloudAuthDataRequest {

  final String accountName;

  final String password;

  final String rememberMe;

  final String[] trustTokens;

  /**
   * The constructor.
   *
   * @param accountName
   * @param password
   * @param rememberMe
   * @param trustTokens
   */
  public ICloudAuthDataRequest(String accountName, String password, String rememberMe, String[] trustTokens) {

    super();
    this.accountName = accountName;
    this.password = password;
    this.rememberMe = rememberMe;
    this.trustTokens = trustTokens;
  }

}
