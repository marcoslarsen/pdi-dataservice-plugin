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

import com.pentaho.det.impl.data.access.jdbc.meta.DataModel;
import com.pentaho.det.impl.data.access.jdbc.meta.DataModelField;
import com.pentaho.det.impl.data.access.jdbc.meta.DataModelFieldStyle;
import com.pentaho.det.impl.data.access.jdbc.sql.SQLConverter;
import org.pentaho.det.api.data.access.IDataSet;
import org.pentaho.det.api.data.access.IDataSourceView;
import org.pentaho.det.api.data.access.meta.IDataModel;
import org.pentaho.det.api.data.access.meta.IDataModelField;
import org.pentaho.det.api.data.access.meta.IDataModelFieldStyle;
import org.pentaho.det.api.data.access.query.IQuery;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.IPhysicalColumn;
import org.pentaho.metadata.model.IPhysicalModel;
import org.pentaho.metadata.model.IPhysicalTable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DataServiceDataSourceView implements IDataSourceView {
  private static final String COLUMN_PROPERTY_MASK = "source_mask";
  private static final String COLUMN_PROPERTY_DECIMAL_SYMBOL = "source_decimalSymbol";
  private static final String COLUMN_PROPERTY_GROUPING_SYMBOL = "source_groupingSymbol";
  private static final String COLUMN_PROPERTY_CURRENCY_SYMBOL = "source_currencySymbol";

  private final Map<UUID, IDataSet> queries = new HashMap<>();
  private IDataModel model;
  private DataServiceDataSource dataSource;

  DataServiceDataSourceView( DataServiceDataSource dataSource ) {
    this.dataSource = dataSource;
  }

  @Override
  public Mode getMode() {
    return IDataSourceView.Mode.STREAM;
  }

  @Override
  public IDataModel getDataModel() {
    if ( this.model == null ) {
      loadDataModel();
    }

    return this.model;
  }

  @Override
  public Map<UUID, IDataSet> getQueryExecutions() {
    return this.queries;
  }

  @Override
  public IDataSet executeQuery(IQuery iQuery) throws IllegalArgumentException {
    UUID uuid = UUID.randomUUID();

    DataServiceDataSet queryExecution = new DataServiceDataSet( uuid, this, iQuery );

    this.queries.put( uuid, queryExecution );

    return queryExecution;
  }

  /**
   * Remove the queryExecution with the passed uuid from the queries map
   *
   * @param uuid The {@link UUID} uuid of the execution to be removed.
   *
   * @return true if removed, false if no query with the passed uuid exists in the queries map.
   */
  public boolean removeExecution( UUID uuid ) {
    if ( !queries.containsKey( uuid ) ) {
      return false;
    }
    queries.remove( uuid );

    return true;
  }

  /**
   * Initialize the view model
   */
  private void loadDataModel() {
    Domain domain = this.dataSource.getDomain();

    if ( domain == null ) {
      return;
    }

    IPhysicalModel model = domain.getPhysicalModels().get( 0 );
    IPhysicalTable table = model.getPhysicalTables().get( 0 );
    List<IPhysicalColumn> columns = table.getPhysicalColumns();

    this.model = new DataModel();
    Collection<IDataModelField> fields = this.model.getFields();

    String locale = this.dataSource.getLocale().toString();
    for ( IPhysicalColumn column : columns ) {
      DataModelField field = new DataModelField();
      field.setName( column.getId() );

      field.setLabel( column.getName( locale ) );
      field.setDescription( column.getDescription( locale ) );

      field.setType( getGoogleDataTableType( column ) );
      field.setFormula( column.getId() );

      field.setHidden( false );
      field.setStyle( getColumnStyle( column ) );

      if ( column.getAggregationType() != null ) {
        field.setDefaultAggregationType( column.getAggregationType().name() );
      }

      fields.add( field );
    }
  }

  /**
   * Gets the column {@link IDataModelFieldStyle style}.
   *
   * @param column - A column from the domain model.
   *
   * @return the column style.
   */
  private IDataModelFieldStyle getColumnStyle( IPhysicalColumn column ) {
    final String mask = (String) column.getProperty( COLUMN_PROPERTY_MASK );

    if ( mask != null ) {
      return new DataModelFieldStyle(
            mask, (String) column.getProperty( COLUMN_PROPERTY_DECIMAL_SYMBOL ),
            (String) column.getProperty( COLUMN_PROPERTY_GROUPING_SYMBOL ),
            (String) column.getProperty( COLUMN_PROPERTY_CURRENCY_SYMBOL ) );
    }

    return null;
  }

  /**
   * Gets the Google Data Table Type for a given Data Type for use in a Stream data source view.
   *
   * @param column - A column from the domain model.
   *
   * @return the column Google Data Table Type.
   */
  String getGoogleDataTableType( IPhysicalColumn column ) {
    switch ( column.getDataType() ) {
      case NUMERIC:
        return "number";
      case DATE:
        return "date";
      case BOOLEAN:
        return "boolean";
      default:
        return "string";
    }
  }

  public DataServiceDataSource getDataSource() {
    return this.dataSource;
  }

  SQLConverter getSqlConverter() {
        return this.dataSource.getSqlConverter();
    }
}
