/**
 */
package org.openhab.model.sitemap;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Chart</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openhab.model.sitemap.Chart#getService <em>Service</em>}</li>
 *   <li>{@link org.openhab.model.sitemap.Chart#getRefresh <em>Refresh</em>}</li>
 *   <li>{@link org.openhab.model.sitemap.Chart#getPeriod <em>Period</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openhab.model.sitemap.SitemapPackage#getChart()
 * @model
 * @generated
 */
public interface Chart extends NonLinkableWidget
{
  /**
   * Returns the value of the '<em><b>Service</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Service</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Service</em>' attribute.
   * @see #setService(String)
   * @see org.openhab.model.sitemap.SitemapPackage#getChart_Service()
   * @model
   * @generated
   */
  String getService();

  /**
   * Sets the value of the '{@link org.openhab.model.sitemap.Chart#getService <em>Service</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Service</em>' attribute.
   * @see #getService()
   * @generated
   */
  void setService(String value);

  /**
   * Returns the value of the '<em><b>Refresh</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Refresh</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Refresh</em>' attribute.
   * @see #setRefresh(int)
   * @see org.openhab.model.sitemap.SitemapPackage#getChart_Refresh()
   * @model
   * @generated
   */
  int getRefresh();

  /**
   * Sets the value of the '{@link org.openhab.model.sitemap.Chart#getRefresh <em>Refresh</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Refresh</em>' attribute.
   * @see #getRefresh()
   * @generated
   */
  void setRefresh(int value);

  /**
   * Returns the value of the '<em><b>Period</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Period</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Period</em>' attribute.
   * @see #setPeriod(String)
   * @see org.openhab.model.sitemap.SitemapPackage#getChart_Period()
   * @model
   * @generated
   */
  String getPeriod();

  /**
   * Sets the value of the '{@link org.openhab.model.sitemap.Chart#getPeriod <em>Period</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Period</em>' attribute.
   * @see #getPeriod()
   * @generated
   */
  void setPeriod(String value);

} // Chart
