package server1;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

public class ChatServerWatchService implements Runnable {
	private String watchPath;
	WatchService service = null;
	
	public ChatServerWatchService(String watchPath) {
		this.watchPath = watchPath;
		try {
			service = FileSystems.getDefault().newWatchService();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			//path 객체를 얻는다
			Path path = Paths.get(watchPath);
			
			//path에 watch service를 등록한다 
			//등록시 감시할 항목을 설정한다
			path.register(service, 
					StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);
			
			while(true) {
				//서비스로 부터 감시 키를 얻는다   
				WatchKey key = service.take();
				
				//경로에 설정한 감시 항목 중 변경 이벤트가 발생하면 이벤트 목록을 얻는다				
				dispatchPollEvents(key.pollEvents());
				
				//키 리셋
				if(!key.reset()) break; 
			}		
			service.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void dispatchPollEvents(List<WatchEvent<?>> list) {
		for(WatchEvent<?> event : list) {
			dispatchWatchEvent(event);
		}		
	}
	
	private void dispatchWatchEvent(WatchEvent<?> event) {
		Kind<?> kind = event.kind();
		Path path = (Path) event.context();
		if(kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
			//파일이 생성되었을 때 실행되는 코드
			System.out.println("생성 : " + path.getFileName());
		} else if(kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
			//파일이 삭제되었을 때 실행되는 코드
			System.out.println("삭제 : " + path.getFileName());
		} else if(kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
			//파일이 수정되었을 때 실행되는 코드
			System.out.println("수정 : " + path.getFileName());
		} else if(kind.equals(StandardWatchEventKinds.OVERFLOW)) {
			//운영체제에서 이벤트가 소실되었거나 버려질 경우 실행되는 코드
			System.out.println("OVERFLOW");
		}
	}
}
