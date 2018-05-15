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
import org.pentaho.di.trans.dataservice.api.data.access.IDataServiceDataSourceFactory;
import org.pentaho.di.trans.dataservice.client.api.IDataServiceClientService;
import org.pentaho.metadata.model.Domain;

public class DataServiceDataSourceFactory implements IDataServiceDataSourceFactory {
  private SQLConverter sqlConverter;

  public DataServiceDataSourceFactory( SQLConverter sqlConverter ) {
        this.sqlConverter = sqlConverter;
    }

  @Override
  public IDataSource createDataSource(IDataServiceClientService client, String dataServiceName, Domain domain,
                                      String label ) {
    final DataServiceDataSource ds = new DataServiceDataSource( client, dataServiceName, domain, label );
    ds.setSqlConverter( this.sqlConverter );
    return ds;
  }

  @Override
  public void disposeDataSource(IDataSource dataSource) {

  }
}
