/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.orc;

import org.apache.flink.core.fs.Path;
import org.apache.flink.table.dataformat.BaseRow;
import org.apache.flink.table.dataformat.ColumnarRow;
import org.apache.flink.table.dataformat.vector.VectorizedColumnBatch;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.orc.TypeDescription;

import java.io.IOException;
import java.util.List;

/**
 * {@link OrcSplitReader} to read ORC files into {@link BaseRow}.
 */
public class OrcColumnarRowSplitReader extends OrcSplitReader<BaseRow> {

	// the vector of rows that is read in a batch
	private final VectorizedColumnBatch columnarBatch;

	private final ColumnarRow row;

	public OrcColumnarRowSplitReader(
			Configuration conf,
			TypeDescription schema,
			int[] selectedFields,
			ColumnBatchGenerator batchGenerator,
			List<Predicate> conjunctPredicates,
			int batchSize,
			Path path,
			long splitStart,
			long splitLength) throws IOException {
		super(
				conf,
				schema,
				selectedFields,
				conjunctPredicates,
				batchSize,
				path,
				splitStart,
				splitLength);

		this.columnarBatch = batchGenerator.generate(rowBatch);
		this.row = new ColumnarRow(columnarBatch);
	}

	@Override
	protected int fillRows() {
		int size = rowBatch.size;
		columnarBatch.setNumRows(size);
		return size;
	}

	@Override
	public BaseRow nextRecord(BaseRow reuse) {
		// return the next row
		row.setRowId(this.nextRow++);
		return row;
	}

	/**
	 * Interface to gen {@link VectorizedColumnBatch}.
	 */
	public interface ColumnBatchGenerator {
		VectorizedColumnBatch generate(VectorizedRowBatch rowBatch);
	}
}
