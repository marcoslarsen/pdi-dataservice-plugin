/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.dataservice.impl.data.access;

import com.pentaho.det.impl.data.access.jdbc.sql.SQLConverter;
import org.pentaho.det.api.data.access.IDataSource;
import org.pentaho.det.api.data.access.IDataSourceView;
import org.pentaho.di.trans.dataservice.client.api.IDataServiceClientService;
import org.pentaho.metadata.model.Domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Supplier;

public class DataServiceDataSource implements IDataSource {
  private final Map<IDataSourceView.Mode, IDataSourceView> views = new HashMap<>();
  private UUID uid;
  private Domain domain;
  private IDataServiceClientService client;
  private String dataServiceName;
  private SQLConverter sqlConverter;
  private Supplier<Locale> localeSupplier;
  private Supplier<TimeZone> timezoneSupplier;

  private final String label;

  /**
   * Constructor for Dataservices Datasource Object.
   *
   * @param client - The Data Service client {@link IDataServiceClientService}
   * @param dataServiceName- The name of the dataservice
   * @param domain         - The {@link Domain} object
   * @param label          - The data source label
   */
  public DataServiceDataSource( IDataServiceClientService client, String dataServiceName, Domain domain,
                                String label ) {
    this.domain = domain;
    this.client = client;
    this.label = label;
    this.dataServiceName = dataServiceName;

    this.localeSupplier = Locale::getDefault;
    this.timezoneSupplier = TimeZone::getDefault;

    // Create PDI data views
    this.views.put( IDataSourceView.Mode.STREAM, new DataServiceDataSourceView( this ) );

  }

  @Override
  public UUID getUuid() {
    return uid;
  }

  @Override
  public void setUuid( UUID uuid ) {
    this.uid = uuid;
  }

  @Override
  public String getType() {
    return "dataservice";
  }

  @Override
  public Map<IDataSourceView.Mode, IDataSourceView> getViews() {
    return this.views;
  }

  @Override
  public Domain getDomain() {
    return domain;
  }

  @Override
  public long getRowCount() {
    return 0;
  }

  @Override
  public String getLabel() {
    return label;
  }

  public IDataServiceClientService getClient() {
    return this.client;
  }

  public String getDataServiceName() {
    return this.getDataServiceName();
  }

  public void setSqlConverter( SQLConverter sqlConverter ) {
    this.sqlConverter = sqlConverter;
  }

  public SQLConverter getSqlConverter() {
    return this.sqlConverter;
  }

  public Locale getLocale() {
        return this.localeSupplier.get();
    }

  public TimeZone getTimezone() {
        return this.timezoneSupplier.get();
    }

  public void setLocaleSupplier( Supplier<Locale> localeSupplier ) {
        this.localeSupplier = localeSupplier;
    }

  public void setTimezoneSupplier( Supplier<TimeZone> timezoneSupplier ) {
        this.timezoneSupplier = timezoneSupplier;
    }
}
