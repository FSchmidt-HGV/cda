/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cda.tests;

//<<<<<<< HEAD
import junit.framework.TestCase;
//=======
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.table.TableModel;

//>>>>>>> 6.0.1.0
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.designtime.datafactory.DesignTimeDataFactoryContext;
import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryRegistry;
import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryCore;
import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryMetaData;
import org.pentaho.reporting.engine.classic.core.util.LibLoaderResourceBundleFactory;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
//<<<<<<< HEAD
//import pt.webdetails.cda.*;
//=======

import pt.webdetails.cda.BaseCdaEnvironment;
import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.ICdaEnvironment;
import pt.webdetails.cda.InitializationException;
//>>>>>>> 6.0.1.0
import pt.webdetails.cda.cache.EHCacheQueryCache;
import pt.webdetails.cda.cache.IQueryCache;
import pt.webdetails.cda.exporter.ExportOptions;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.utils.mondrian.CompactBandedMDXDataFactory;
import pt.webdetails.cda.utils.mondrian.ExtBandedMDXDataFactory;
import pt.webdetails.cda.utils.mondrian.ExtDenormalizedMDXDataFactory;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.PluginSettings;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.bean.AbstractBeanFactory;
import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.plugincall.api.IPluginCall;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;

import javax.swing.table.TableModel;
import java.io.ByteArrayOutputStream;
import java.util.Locale;
import java.util.TimeZone;

//import org.pentaho.reporting.libraries.base.config.Configuration;
//import org.pentaho.reporting.libraries.base.config.HierarchicalConfiguration;


public abstract class CdaTestCase extends TestCase {

  private CdaTestEnvironment testEnvironment;
  private static final String USER_DIR = System.getProperty( "user.dir" );
  private static final Class[] customDataFactories = {
      CompactBandedMDXDataFactory.class, ExtBandedMDXDataFactory.class, ExtDenormalizedMDXDataFactory.class };

  public CdaTestCase( String name ) {
    super( name );
  }

  public CdaTestCase() {
  }

  protected void setUp() throws Exception {
    super.setUp();
    CdaTestingContentAccessFactory factory = new CdaTestingContentAccessFactory();
    log().info( "factory:" + factory );
    // always need to make sure there is a plugin environment initialized
    PluginEnvironment.init( new CdaPluginTestEnvironment( factory ) );

    // cda-specific environment
    testEnvironment = new CdaTestEnvironment( factory );
    // cda init
    CdaEngine.init( testEnvironment );
    // making sure the custom data factories are registered
    registerCustomDataFactories();
    // due to http://jira.pentaho.com/browse/PDI-2975
    System.setProperty( "org.osjava.sj.root", getSimpleJndiPath() );
  }

  protected String getSimpleJndiPath() {

    if ( USER_DIR.endsWith( "bin/test/classes" ) ) {
      // command-line run
      return USER_DIR + File.separator + "simplejndi";
    } else {
      // IDE run
      return USER_DIR + File.separator + "test-resources" + File.separator + "simplejndi";
    }

  }

  protected Log log() {
    return LogFactory.getLog( getClass() );
  }

  protected SettingsManager getSettingsManager() {
    return getEngine().getSettingsManager();
  }

  protected CdaEngine getEngine() {
    return CdaEngine.getInstance();
  }

  protected CdaSettings parseSettingsFile( String cdaSettingsId ) throws Exception {
    return getSettingsManager().parseSettingsFile( cdaSettingsId );
  }

  protected ICdaEnvironment getEnvironment() {
    return CdaEngine.getEnvironment();
  }

  protected TableModel doQuery( CdaSettings cdaSettings, QueryOptions queryOptions ) throws Exception {
    return getEngine().doQuery( cdaSettings, queryOptions );
  }

  protected String exportTableModel( TableModel table, ExportOptions opts ) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    getEngine().getExporter( opts ).export( baos, table );
    return Util.toString( baos.toByteArray() );
  }

  //  protected void setConfigProperty( String property, String value ) {
  //    testEnvironment.setProperty( property, value );
  //  }
  //  protected void resetProperties() {
  //    testEnvironment.resetProperties();
  //  }

  protected static class CdaTestEnvironment extends BaseCdaEnvironment implements ICdaEnvironment {

    //    private HierarchicalConfiguration testConfig;
    private CdaTestingContentAccessFactory factory;

    public CdaTestEnvironment( CdaTestingContentAccessFactory factory ) throws InitializationException {
      super( new AbstractBeanFactory() {
        @Override
        public String getSpringXMLFilename() {
          return "cda.spring.xml";
        }
      } );
      this.factory = factory;
    }

    public void initializeDataFactory( DataFactory dataFactory, Configuration configuration, ResourceKey contextKey,
                                       ResourceManager resourceManager ) throws ReportDataFactoryException {
      dataFactory.initialize( new DesignTimeDataFactoryContext( configuration, resourceManager, contextKey,
          new LibLoaderResourceBundleFactory( resourceManager, contextKey, Locale.getDefault(),
          TimeZone.getDefault() ), dataFactory ) );
    }

    @Override
    public IQueryCache getQueryCache() {
      return new EHCacheQueryCache( false );
    }

    public IContentAccessFactory getRepo() {
      return factory;
    }
    //    public Configuration getBaseConfig() {
    //      return testConfig;
    //    }
    //
    //    public void setProperty(String property, String value) {
    //      testConfig.setConfigProperty( property, value );
    //    }
    //
    //    public void resetProperties() {
    //      testConfig = new HierarchicalConfiguration( CdaBoot.getInstance().getGlobalConfig() );
    //    }

    @Override
    public Locale getLocale() {
      return Locale.getDefault();
    }
  }

  protected static void registerCustomDataFactories() {
    for ( Class clazz : customDataFactories ) {
      DefaultDataFactoryMetaData dmd = new DefaultDataFactoryMetaData(
          clazz.getName(), "", "", true, false, true, false, false, false, false, false,
          new DefaultDataFactoryCore(), 0 );
      DataFactoryRegistry.getInstance().register( dmd );
    }
  }

  protected static class CdaPluginTestEnvironment extends PluginEnvironment {

    private CdaTestingContentAccessFactory factory;

    public CdaPluginTestEnvironment( CdaTestingContentAccessFactory factory ) {
      this.factory = factory;
    }

    public IContentAccessFactory getContentAccessFactory() {
      return factory;
    }

    public IUrlProvider getUrlProvider() {
      throw new UnsupportedOperationException();
    }

    public PluginSettings getPluginSettings() {
      throw new UnsupportedOperationException();
    }

    public String getPluginId() {
      return "cda";
    }

    public IPluginCall getPluginCall( String pluginId, String service, String method ) {
      throw new UnsupportedOperationException();
    }

  }
}
