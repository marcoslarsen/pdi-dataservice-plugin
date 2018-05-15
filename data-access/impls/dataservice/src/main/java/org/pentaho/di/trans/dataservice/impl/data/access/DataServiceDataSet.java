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

import com.pentaho.det.impl.data.access.jdbc.meta.QueryResultStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.det.api.data.access.IDataSet;
import org.pentaho.det.api.data.access.query.IQuery;
import org.pentaho.det.api.data.access.query.streaming.IStreaming;
import org.pentaho.det.api.data.access.result.IQueryResult;
import org.pentaho.di.trans.dataservice.client.api.IDataServiceClientService;

import java.io.DataInputStream;
import java.sql.SQLException;
import java.util.UUID;

public class DataServiceDataSet implements IDataSet {
  private UUID uid;
  private IDataSet.Status status;
  private DataServiceDataSourceView dataView;
  private Log logger = LogFactory.getLog( this.getClass() );
  private IQuery query;
  private final String baseSqlQuery;

  public DataServiceDataSet( UUID uid, DataServiceDataSourceView dataView, IQuery query ) {
    this.uid = uid;
    this.dataView = dataView;
    this.query = query;
    this.status = Status.READY;
    this.baseSqlQuery = this.dataView.getSqlConverter()
      .queryToSql( this.query, this.dataView.getDataSource().getDataServiceName(), this.dataView.getDataModel()
    );
  }

  @Override
  public UUID getUuid() {
    return uid;
  }

  @Override
  public IQueryResult getResult() {
    return getResult( -1, 0 );
  }

  @Override
  public IQueryResult getResult( int limit, int offset ) {
    QueryResultStream queryResult = null;

    IDataServiceClientService dsClient = this.dataView.getDataSource().getClient();

    IStreaming streamingParams = query.getStreamingParams();

    DataInputStream stream = null;

    try {
      if ( streamingParams != null ) {
        IDataServiceClientService.StreamingMode mode =
                IDataServiceClientService.StreamingMode.valueOf( streamingParams.getWindowMode() );

        stream = dsClient.query( baseSqlQuery, mode, streamingParams.getWindowSize(), streamingParams.getWindowEvery(),
                streamingParams.getWindowLimit() );
      } else {
        stream = dsClient.query( baseSqlQuery, -1 );
      }
    } catch ( SQLException e ) {
        e.printStackTrace();
    }
    queryResult = new QueryResultStream( stream );

    return queryResult;
  }

  @Override
  public Status getStatus() {
    return this.status;
  }

  @Override
  public boolean cancel() {
    setStatus( IDataSet.Status.CANCELED );

    return dataView.removeExecution( getUuid() );
  }

 /**
  * Setter for the status.
  *
  * @param status
  */
  void setStatus( IDataSet.Status status ) {
    this.status = status;
  }
}
