/**
  * Copyright 2014 Google Inc. All rights reserved.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
*/

package com.google.mr4c.sources;

import com.google.mr4c.util.MR4CLogging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import org.apache.hadoop.fs.BlockLocation;

public class InMemoryFileSource implements FileSource {

	private Map<String,byte[]> m_files = Collections.synchronizedMap( new HashMap<String,byte[]>() );

	public InMemoryFileSource() {}

	public List<String> getAllFileNames() throws IOException {
		List<String> names = new ArrayList<String>(m_files.keySet());
		Collections.sort(names);
		return names;
	}

	public DataFileSource getFileSource(String fileName) throws IOException {
		return new InMemoryDataFileSource(fileName);
	}

	public boolean fileExists(String fileName) throws IOException {
		return m_files.containsKey(fileName);
	}

	public DataFileSource getFileSourceOnlyIfExists(String fileName) throws IOException {
		return fileExists(fileName) ?
			getFileSource(fileName) :
			null;
	}

	public DataFileSink getFileSink(String fileName) throws IOException {
		return new InMemoryDataFileSink(fileName);
	}

	public void close() {}

	public void ensureExists() {}

	public void clear() {
		m_files.clear();
	}

	public String getDescription() {
		return "In memory file source";
	}

	private String getFileDescription(String fileName) {
		return "In memory file " + fileName;
	}

	private class InMemoryDataFileSource extends AbstractDataFileSource {
		private String m_name;
	
		public InMemoryDataFileSource(String name) {
			m_name = name;
		}

		public long getFileSize() throws IOException {
			return getFileBytes().length;
		}
	
		public InputStream getFileInputStream() throws IOException {
			byte[] bytes = getFileBytes();
			return new ByteArrayInputStream(bytes);
		}
	
		public byte[] getFileBytes() throws IOException {
			byte[] bytes = m_files.get(m_name);
			if ( bytes==null ) {
				throw new FileNotFoundException(String.format("No file named [%s]", m_name));
			}
			return bytes;
		}
	
		public void getFileBytes(ByteBuffer buf) throws IOException {
			byte[] bytes = getFileBytes();
			buf.put(bytes);
		}
			
	
		public void release() {}
	
		public String getDescription() {
			return getFileDescription(m_name);
		}

	}


	class InMemoryDataFileSink extends AbstractDataFileSink {
	
		private String m_name;
	
		public InMemoryDataFileSink(String name) {
			m_name = name;
		}
	
		public OutputStream getFileOutputStream() throws IOException {
			return new ByteArrayOutputStream() {
				@Override public void close() {
					writeFile(toByteArray());
				}
			};
		}
	
		public void writeFile(byte[] bytes) {
			m_files.put(m_name,bytes);
		}
	
		public void writeFile(InputStream input) throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(input, baos);
			writeFile(baos.toByteArray());
		}

		public String getDescription() {
			return getFileDescription(m_name);
		}

	}



}

