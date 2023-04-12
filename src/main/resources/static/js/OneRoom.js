//////////////////////////////////////////////////// 소켓 구역 ////////////////////////////////////////////////////
// 1. SockJS를 생성한다. (StompWebSocketConfig에서 설정한 Endpoint와 동일하게 맞춰준다.)
var sockJs = new SockJS("/ws/meta/chat");
// 2. 생성된 SockJS를 Stomp에 전달한다.
var stomp = Stomp.over(sockJs);

// 3. connect가 이뤄지면 실행한다.
stomp.connect({}, function () {
    // connect가 이뤄지면 콘솔에 로그를 찍는다.
    console.log("STOMP Connection");

    // 5. subscribe(path, callback)으로 메시지를 받을 수 있다.
    //    StompChatController에서 SimpMessagingTemplate를 통해 전달한 DTO를 여기서 콜백 메소드 파라미터로 전달 받는다.
    stomp.subscribe("/sub/meta/oneroom/" + metaIdx, function (chat) {

    }); // stomp
    ////////////////////////////////////////////////// 입장 구역 //////////////////////////////////////////////////
    // 입장 시작!! - 먼저 입장 체크값을 이용하여 해당 유저가 첫 입장인지 재입장(새로고침)인지 체크한다.
    // 첫 입장일 경우 - 입장 체크값이 존재하지 않는다.
    if ( entryCheck == null ) {
        // 시작 시간과 메시지 전송 시간과 전송된 메시지 수와 메시지 전송 상태의 각 변수명을 키로 사용하고, 각 초기값을 지정해 값으로 사용하여 로컬 스토리지에 추가한다.
        localStorage.setItem("start", Date.now()); // Date.now()로 초기화
        localStorage.setItem("end", Date.now()); // Date.now()로 초기화
        localStorage.setItem("count", 0); // 0으로 초기화
        localStorage.setItem("sendMessage", "true"); // true로 초기화
        // 로컬 스토리지에 추가한 값들을 가져와 각 알맞는 변수에 전달한다.
        start = localStorage.getItem("start");
        end = localStorage.getItem("end");
        count = localStorage.getItem("count");
        sendMessage = localStorage.getItem("sendMessage");
        // 4번이 5번보다 아래에 위치한 이유 - 위에 있을경우 간혹 4번에서 전송한 메시지를 제대로 전달받지 못하는 경우가 존재한다.
        // 4. send(path, header, message)로 입장 메시지를 전송한다. (첫 입장할때 딱 한번만 여기서 입장 메시지를 전송한다.)
        //    JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
        //    여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
        stomp.send('/pub/meta/oneroom/enter', {}, JSON.stringify({type: "enter", metaIdx: metaIdx, writer: metaNickname, metaRecruitingPersonnel: metaRecruitingPersonnel}));
    // 재입장(새로고침)일 경우 - 입장 체크값이 존재한다.
    } else {
        // 4번이 5번보다 아래에 위치한 이유 - 위에 있을경우 간혹 4번에서 전송한 메시지를 제대로 전달받지 못하는 경우가 존재한다.
        // 4. send(path, header, message)로 입장 메시지를 전송한다. (첫 입장 이후 모든 재입장(새로고침)은 여기서 입장 메시지를 전송한다.)
        //    JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
        //    여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
        stomp.send('/pub/meta/oneroom/reenter', {}, JSON.stringify({type: "reEnter", metaIdx: metaIdx, writer: metaNickname, metaRecruitingPersonnel: metaRecruitingPersonnel}));
    }
});
////////////////////////////////////////////////// 녹음 변수 구역 //////////////////////////////////////////////////
let record = document.getElementById("record"); // 오디오 태그
let btnRecordStart = document.getElementById("btnRecordStart"); // 녹음 시작 버튼
let btnRecordStop = document.getElementById("btnRecordStop"); // 녹음 정지 버튼
let btnRecordDownload = document.getElementById("btnRecordDownload"); // 녹음 다운로드 버튼
let mediaRecorder; // MediaRecorder 객체
let recordedChunks = []; // blob 녹음 데이터
let recordTimeoutId; // 녹음 setTimeout ID
let recordBlob; // 병합한 Blob 녹음 데이터
let recordUrl; // 병합한 Blob 녹음 데이터의 URL
//////////////////////////////////////////////////// 녹음 구역 ////////////////////////////////////////////////////
// 녹음 시작 버튼 클릭 시 호출되는 메소드
async function startRecording() {
    // navigator.mediaDevices.getUserMedia() - 미디어 스트림(비디오 또는 오디오)을 생성하는 메소드로 비동기 메소드이며, 사용자의 미디어 장치(예: 마이크)를 사용할 수 있는 권한이 있는지를 확인하고, 권한이 있다면 미디어 스트림을 반한다.
    // getUserMedia() 메소드는 mediaDevices 객체에 호출되며, 사용자에게 미디어 장치 사용 권한을 요청한다.
    // getUserMedia()에 전달된 매개변수는 'audio' 속성을 true로 설정하여 사용자의 오디오 장치에 액세스하려는 것을 지정한다.
    // getUserMedia()는 Promise 객체를 반환하며,
    // 요청한 미디어 타입에 대한 스트림을 성공적으로 가져올 경우 then() 메소드를 호출하여 성공적인 MediaStream 객체를 반환한다.
    // 반면, 접근 권한이 거부되거나 미디어 장치가 없는 경우 catch() 메소드를 호출하여 NotAllowedError 또는 NotFoundError 오류를 반환한다.
    // getUserMedia()는 안전한 출처 (즉, localhost, HTTPS)에서만 작동한다.
    navigator.mediaDevices.getUserMedia({ audio: true })
        // then 메소드는 Promise 객체가 resolve된 후 호출된다.
        .then(stream => {
            try {
                // 브라우저가 WAV 오디오 형식을 지원하는지 검사한다.
                // WAV 오디오 형식을 지원하는 경우
                if ( typeof MediaRecorder.isTypeSupported == "function" && MediaRecorder.isTypeSupported( "audio/wav" ) ) {
                    // MediaRecorder 생성자를 사용하여 입력 스트림(stream)에서 오디오 데이터를 캡처하고,
                    // 해당 데이터의 MIME 타입을 "audio/wav"로 설정하는 MediaRecorder 객체를 생성해 mediaRecorder 변수에 전달한다.
                    // stream(입력 스트림) - getUserMedia() 메소드로 얻은 스트림을 저장하는 변수이며, 이를 MediaRecorder 생성자에 전달하여 오디오 데이터를 캡처한다.
                    mediaRecorder = new MediaRecorder(stream, { mimeType: "audio/wav" });
                // WAV 오디오 형식을 지원하지 않는 경우
                } else {
                    // MediaRecorder 생성자를 사용하여 입력 스트림(stream)에서 오디오 데이터를 캡처하고,
                    // 해당 데이터의 MIME 타입을 "audio/webm"으로 설정하는 MediaRecorder 객체를 생성해 mediaRecorder 변수에 전달한다.
                    // stream(입력 스트림) - getUserMedia() 메소드로 얻은 스트림을 저장하는 변수이며, 이를 MediaRecorder 생성자에 전달하여 오디오 데이터를 캡처한다.
                    mediaRecorder = new MediaRecorder(stream, { mimeType: "audio/webm" });
                }

                // MediaRecorder 객체에서 "dataavailable" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
                // dataavailable - MediaRecorder 객체에서 발생하는 이벤트 중 하나로 MediaRecorder가 녹음을 진행하면서 데이터를 누적할 때마다 발생하는 이벤트이다.
                //                 즉, 녹음된 오디오 데이터가 사용 가능해질 때마다 이 이벤트가 발생한다.
                //                 이 때 발생한 녹음된 오디오 데이터는 Blob 형태로 event.data에 담긴다.
                //                 그러기에 이 이벤트를 이용하면 MediaRecorder가 녹음한 오디오 데이터를 직접 다룰 수 있게 된다.
                mediaRecorder.addEventListener("dataavailable", function(event) {
                    // event.data에 들어있는 녹음된 오디오 데이터가 유효한지 체크한다.
                    // 녹음된 오디오 데이터가 유효하지 않은 경우
                    if (typeof event.data === "undefined") {
                        // 아무 작업 없이 돌아간다.
                        return;
                    // 녹음된 오디오 데이터가 유효한 경우
                    } else {
                        // 녹음된 오디오 데이터를 위에서 미리 만들어둔 recordedChunks 배열에 추가한다.
                        recordedChunks.push(event.data);
                        // Blob 생성자를 사용하여 recordedChunks 배열에 저장된 Blob 오디오 데이터들을 모두 가져와,
                        // MIME 타입을 "audio/webm"으로 설정하는 하나의 Blob 객체로 재생성해,
                        // recordBlob 변수에 전달한다.
                        recordBlob = new Blob(recordedChunks, { type: "audio/webm" });
                        // createObjectURL 메소드를 사용하여 재생성된 Blob 객체를 URL 객체로 변환해,
                        // recordUrl 변수에 전달한다.
                        recordUrl = URL.createObjectURL(recordBlob);
                        // .src를 사용하여 변환된 URL을 id값으로 가져온 현재 작성되어 있는 오디오 태그의 소스로 설정해,
                        // 녹음된 오디오를 전송하기 전에 녹음 내용을 들어 볼 수 있게 한다.
                        record.src = recordUrl;
                    }
                });

                // MediaRecorder의 start() 메소드를 호출하여 녹음을 시작한다.
                mediaRecorder.start();

                // setTimeout 메소드를 사용하여 녹음이 시작된지 5분이 경과하면 자동으로 녹음이 멈추고,
                // 이후 녹음을 더 이상 할 수 없도록 막고 초기화와 전송만 가능하도록 한다.
                // 이는 녹음 최대 시간이 5분을 넘지 않도록 하는 것이다.
                // 그리고 recordTimeoutId 변수에 setTimeout 메소드로 반환된 타이머 ID를 저장하여,
                // 타이머가 실행되는 동안 recordTimeoutId로 언제든지 타이머를 취소할 수 있도록 한다.
                recordTimeoutId = setTimeout(function() {
                    // MediaRecorder의 stop() 메소드를 호출하여 녹음을 멈춘다.
                    mediaRecorder.stop();
                    // 초기화 버튼을 활성화한다.
                    btnRecordStart.disabled = false;
                    // 재시작 버튼을 비활성화한다.
                    btnRecordStop.disabled = true;
                    // 다운로드 버튼을 활성화한다.
                    btnRecordDownload.disabled = false;
                }, 300000);
            // catch 메소드는 Promise 객체가 reject된 후 호출된다.
            } catch (error) {
                // 오류가 발생한 경우, 해당 오류를 콘솔에 출력한다.
                console.error(error);
            }
    });
}

// 오디오 태그에서 "ended" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
// ended - 오디오나 비디오 등의 미디어가 재생이 끝났을 때 발생하는 이벤트이다.
record.addEventListener("ended", function() {
    // 오디오 태그의 재생이 종료되면, 오디오 태그에 작성되어 있는 소스를 그대로 재작성한다.
    // 이는 오디오 재생이 끝나고 다시 재생할 때, 오디오 소스가 초기화되도록 하는 역할을 한다.
    // 이렇게 하는 이유는, 오디오 태그의 재생이 종료되고 나서 다시 오디오 태그가 재생될 때, 병합된 오디오 데이터를 온전히 다시 재생하기 위해서이다.
    // 이렇게 하지 않으면, 오디오 태그의 재생이 종료되고 나서 다시 오디오 태그가 재생될 때, 병합된 오디오 데이터중 마지막 오디오 데이터만 재생된다.
    // 하나의 오디오 데이터는 문제가 없지만, 병합된 여러개의 오디오 데이터는 이런 문제가 발생해 임시방편으로 찾아낸 방법이다.
    record.src = record.src;
});

// 시작 및 초기화 버튼에서 "click" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
// click - 마우스나 터치 디바이스의 클릭이나 탭을 할 때 발생하는 이벤트이다.
btnRecordStart.addEventListener("click", function() {
    // 시작 버튼 이름을 체크한다.
    // "녹음 시작" 버튼을 클릭한 경우
    if ( btnRecordStart.value == "녹음 시작" ) {
        // 녹음 시작 메소드를 호출하여 녹음을 시작한다.
        startRecording();
        // 시작 버튼을 초기화 버튼으로 변경한다.
        btnRecordStart.value = "녹음 초기화";
        // 초기화 버튼을 비활성화한다.
        btnRecordStart.disabled = true;
        // 정지 버튼을 활성화한다.
        btnRecordStop.disabled = false;
        // 다운로드 버튼을 비활성화한다.
        btnRecordDownload.disabled = true;
    // "녹음 초기화" 버튼을 클릭한 경우
    } else {
        // 초기화 버튼을 시작 버튼으로 변경한다.
        btnRecordStart.value = "녹음 시작";
        // 시작 버튼을 활성화한다.
        btnRecordStart.disabled = false;
        // 재시작 버튼을 정지 버튼으로 변경한다.
        btnRecordStop.value = "녹음 정지";
        // 정지 버튼을 비활성화한다.
        btnRecordStop.disabled = true;
        // 다운로드 버튼을 비활성화한다.
        btnRecordDownload.disabled = true;
        // recordedChunks 배열을 초기화한다.
        recordedChunks = [];
        // 현재 오디오 태그에 작성되있는 소스를 초기화한다.
        record.src = "";
    }
});

// 정지 및 재시작 버튼에서 "click" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
// click - 마우스나 터치 디바이스의 클릭이나 탭을 할 때 발생하는 이벤트이다.
btnRecordStop.addEventListener("click", function() {
    // 정지 버튼 이름을 체크한다.
    // "녹음 정지" 버튼을 클릭한 경우
    if ( btnRecordStop.value == "녹음 정지" ) {
        // recordTimeoutId에 저장된 setTimeout ID를 사용하여 실행 중인 타이머를 취소한다.
        clearTimeout(recordTimeoutId);
        // MediaRecorder의 stop() 메소드를 호출하여 녹음을 멈춘다.
        mediaRecorder.stop();
        // 시작 버튼을 초기화 버튼으로 변경한다.
        btnRecordStart.value = "녹음 초기화";
        // 초기화 버튼을 활성화한다.
        btnRecordStart.disabled = false;
        // 정지 버튼을 재시작 버튼으로 변경한다.
        btnRecordStop.value = "녹음 재시작";
        // 재시작 버튼을 활성화한다.
        btnRecordStop.disabled = false;
        // 다운로드 버튼을 활성화한다.
        btnRecordDownload.disabled = false;
    // "녹음 재시작" 버튼을 클릭한 경우
    } else {
        // 오디오 태그에 작성되있는 소스를 초기화한다.
        record.src = "";
        // 병합한 Blob 녹음 데이터의 URL을 초기화한다.
        recordUrl = "";
        // 녹음 시작 메소드를 호출하여 녹음을 재시작한다.
        startRecording();
        // 초기화 버튼을 비활성화한다.
        btnRecordStart.disabled = true;
        // 재시작 버튼을 정지 버튼으로 변경한다.
        btnRecordStop.value = "녹음 정지";
        // 정지 버튼을 활성화한다.
        btnRecordStop.disabled = false;
        // 다운로드 버튼을 비활성화한다.
        btnRecordDownload.disabled = true;
    }
});

// 다운로드 버튼에서 "click" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
// click - 마우스나 터치 디바이스의 클릭이나 탭을 할 때 발생하는 이벤트이다.
btnRecordDownload.addEventListener("click", function() {
    // a 태그를 생성한다. - 다운로드 태그 생성
    let recordDownload = document.createElement("a");
    // 오디오 태그의 소스에서 녹음된 오디오 데이터를 가져와 a 태그의 소스로 전달한다. - 다운로드 링크 생성
    recordDownload.href = document.getElementById("record").src;
    // a 태그의 download 속성을 사용하여 다운로드 파일의 이름을 설정한다. - 다운로드 링크의 파일 이름 설정
    recordDownload.download = "recordAudio.mp3";
    // 생성된 a 태그에 클릭 이벤트를 발생시킨다. - 다운로드 시작
    recordDownload.click();
});
///////////////////////////////////////////////// 페이지 이탈 구역 /////////////////////////////////////////////////
// 1. 페이지를 이탈하는 기능을 실행할 경우 발생하는 이벤트 핸들러 등록
// beforeunload - 탭 닫기, 윈도우 닫기, 페이지 닫기, 뒤로가기, 버튼, location.href, 새로고침 등 해당 페이지를 벗어나는 기능을 실행할 경우 항상 실행된다.
window.addEventListener("beforeunload", handleBeforeUnload);
// beforeunload 핸들러 메소드
function handleBeforeUnload(event) {
    // beforeunload 이벤트를 명시적으로 처리하지 않은 경우, 해당 이벤트에 기본 동작을 실행하지 않도록 지정한다.
    event.preventDefault();
    // beforeunload 경고창을 띄워준다. - 따로 메시지 작성을 안한 이유는 각 브라우저마다 기본으로 잡혀있는 메시지가 표시되기 때문이다.
    event.returnValue = "";
}

// 2. beforeunload 이벤트에서 반환한 경고창에 따라 <body>태그에 작성한 unload 이벤트에서 확인 및 취소를 체크하고, 확인을 누른 경우에만 지정한 메소드로 이동시킨다.
// unload - 어떤 방식으로든 페이지를 이탈하면 항상 실행된다.

// 3. 페이지 이탈 후 퇴장 메시지 전송 메소드 - unload에서 지정한 메소드
function exit() {
    // send(path, header, message)로 퇴장 메시지를 전송한다. (방장이 퇴장할때 딱 한번만 전송한다.)
    // JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
    // 여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
    stomp.send('/pub/meta/oneroom/exit', {}, JSON.stringify({type: "exit", metaIdx: metaIdx, writer: metaNickname, metaRecruitingPersonnel: metaRecruitingPersonnel}));
}