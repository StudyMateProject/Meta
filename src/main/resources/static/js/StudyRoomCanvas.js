//////////////////////////////////////////////////// 소켓 구역 ////////////////////////////////////////////////////
// 1. SockJS를 생성한다. (StompWebSocketConfig에서 설정한 Endpoint와 동일하게 맞춰준다.)
var canvasSockJs = new SockJS("/ws/meta/canvas");
// 2. 생성된 SockJS를 Stomp에 전달한다.
var canvasStomp = Stomp.over(canvasSockJs);

// 3. connect가 이뤄지면 실행한다.
canvasStomp.connect({}, function () {
    // connect가 이뤄지면 콘솔에 로그를 찍는다.
    console.log("canvasStomp Connection");

    // 5. subscribe(path, callback)으로 메시지를 받을 수 있다.
    //    StompChatController에서 SimpMessagingTemplate를 통해 전달한 DTO를 여기서 콜백 메소드 파라미터로 전달 받는다.
    canvasStomp.subscribe("/sub/meta/studyroom/canvas/" + canvasMetaIdx, function (message) {
        // 6. JSON.parse(변환 대상) - JSON 문자열을 JavaScript 값이나 객체로 변환한다.
        //    JSON형식으로 넘어온 DTO를 JavaScript형식으로 변환한다.
        var canvasContent = JSON.parse(message.body);
        // 6-1. 변환된 DTO를 사용하기 편하게 각각 변수에 나눠놓는다.
        var canvasWriter = canvasContent.writer; // 작성자 닉네임
        var canvasType = canvasContent.type; // 메시지 타입

        // 7. 메시지 타입 값에 따라 나눈다.

        // 7-1. 메시지 타입이 "enter"일 경우 / 메시지 타입이 "reEnter"일 경우
        // 첫 입장 / 재입장
        if ( canvasType == "enter" || canvasType == "reEnter" ) {
            serverCharacters = canvasContent.characters; // 캐릭터 Map - 서버로 보내기용
            canvasCharacters = JSON.parse(serverCharacters); // 캐릭터 Map 파싱 - 캔버스에 그리기용
            // 작성자 닉네임과 로그인 유저 닉네임이 같은 경우 (본인)
            if ( canvasWriter === canvasNickname ) {
                // 캔버스 시작 메소드로 이동한다.
                // 캔버스를 시작한다.
                canvasStart();
            // 작성자 닉네임과 로그인 유저 닉네임이 다른 경우 (타 유저)
            } else {
                // 캐릭터 그리기 메소드로 이동한다.
                // 타 유저의 캐릭터 Map으로 다시 그린다.
                drawChar();
            }
        }

        // 7-2. 메시지 타입이 "reErr"일 경우
        // 재입장(새로고침) 에러
        if ( canvasType == "reErr" ) {
            // 작성자 닉네임과 로그인 유저 닉네임이 같은 경우 (본인)
            if ( canvasWriter == canvasNickname ) {
                // 재입장 에러로 입장 메시지 재전송
                if ( writer === metaNickname ) {
                    canvasStomp.send('/pub/meta/studyroom/canvas/enter', {}, JSON.stringify({type: "enter", metaIdx: canvasMetaIdx, writer: canvasNickname, character: canvasProfileImage, x : cw / 2, y : (ch / 6) * 5}));
                }
            }
        }

        // 7-3. 메시지 타입이 "left"일 경우 / 메시지 타입이 "top"일 경우 / 메시지 타입이 "right"일 경우 / 메시지 타입이 "bottom"일 경우
        // 왼쪽 이동 / 위쪽 이동 / 오른쪽 이동 / 아래쪽 이동
        if ( canvasType == "left" || canvasType == "top" || canvasType == "right" || canvasType == "bottom" ) {
            serverCharacters = canvasContent.characters; // 캐릭터 Map - 서버로 보내기용
            canvasCharacters = JSON.parse(serverCharacters); // 캐릭터 Map 파싱 - 캔버스에 그리기용
            // 캐릭터 그리기 메소드로 이동한다.
            // 이동한 좌표의 캐릭터 Map으로 다시 그린다.
            drawChar();
        }

        // 7-4. 메시지 타입이 "delete"일 경우 / 메시지 타입이 "exit"일 경우
        // 방 퇴장
        if ( canvasType == "delete" || canvasType == "exit" ) {
            serverCharacters = canvasContent.exit; // 퇴장한 유저를 제외한 캐릭터 Map - 서버로 보내기용
            canvasCharacters = JSON.parse(serverCharacters); // 퇴장한 유저를 제외한 캐릭터 Map 파싱 - 캔버스에 그리기용
            // 캐릭터 그리기 메소드로 이동한다.
            // 퇴장한 유저를 제외한 캐릭터 Map으로 다시 그린다.
            drawChar();
        }

        if ( canvasType == "KickOut" ) {
            serverCharacters = canvasContent.exit; // 퇴장한 유저를 제외한 캐릭터 Map - 서버로 보내기용
            canvasCharacters = JSON.parse(serverCharacters); // 퇴장한 유저를 제외한 캐릭터 Map 파싱 - 캔버스에 그리기용
            // 캐릭터 그리기 메소드로 이동한다.
            // 퇴장한 유저를 제외한 캐릭터 Map으로 다시 그린다.
            drawChar();
        }
    });
    ////////////////////////////////////////////////// 입장 구역 //////////////////////////////////////////////////
    // 입장 시작!! - 먼저 입장 체크값을 이용하여 해당 유저가 첫 입장인지 재입장(새로고침)인지 체크한다.
    // 첫 입장일 경우 - 입장 체크값이 존재하지 않는다.
    if ( canvasEntryCheck == null ) {
        // 4번이 5번보다 아래에 위치한 이유 - 위에 있을경우 간혹 4번에서 전송한 메시지를 제대로 전달받지 못하는 경우가 존재한다.
        // 4. send(path, header, message)로 입장 메시지를 전송한다. (첫 입장할때 딱 한번만 여기서 입장 메시지를 전송한다.)
        //    JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
        //    여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
        canvasStomp.send('/pub/meta/studyroom/canvas/enter', {}, JSON.stringify({type: "enter", metaIdx: canvasMetaIdx, writer: canvasNickname, character: canvasProfileImage, x : cw / 2, y : (ch / 6) * 5}));
    // 재입장(새로고침)일 경우 - 입장 체크값이 존재한다.
    } else {
        // 4번이 5번보다 아래에 위치한 이유 - 위에 있을경우 간혹 4번에서 전송한 메시지를 제대로 전달받지 못하는 경우가 존재한다.
        // 4. send(path, header, message)로 입장 메시지를 전송한다. (첫 입장 이후 모든 재입장(새로고침)은 여기서 입장 메시지를 전송한다.)
        //    JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
        //    여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
        canvasStomp.send('/pub/meta/studyroom/canvas/reenter', {}, JSON.stringify({type: "reEnter", metaIdx: canvasMetaIdx, writer: canvasNickname, character: canvasProfileImage, x : cw / 2, y : (ch / 6) * 5}));
    }
});
/////////////////////////////////////////////////// 캔버스 구역 ///////////////////////////////////////////////////
// 캔버스 메소드
function canvasStart() {
    // canvas id값으로 가져오기
    canvas = document.getElementById("canvas");
    // canvas를 2d로 설정
    context = canvas.getContext("2d");
    // canvas 크기 설정
    setCanvasSize();

    // 캔버스 크기의 절대적 위치
    canvasLeft = 0 + 50; // 왼쪽 벽
    canvasTop = 0 + 50 // 위쪽 벽
    canvasRight = document.getElementById("canvas").clientWidth - 50; // 오른쪽 벽
    canvasBottom = document.getElementById("canvas").clientHeight - 50; // 아래쪽 벽

    // 시작!!
    //runGame();
    // 시작 알람
    // 실행되고있는 AnimationFrame 제거 - 이 작업을 먼저 안하면 실행중인 AnimationFrame이 제거되지 않고 계속 쌓이게 된다.
    cancelAnimationFrame(runGame);
    // AnimationFrame 실행 (콜백 메소드) - AnimationFrame으로 실행시킬 메소드를 괄호에 넣는다.
    requestAnimationFrame(runGame);
    // 구형 프레임 작동방식 - 그림을 수시로 그려내는 방식으로 로직이 복잡해질시 프레임이 밀려 캐릭터가 버벅거린다.
    // setInterval(runGame);
}

// canvas 크기 설정 메소드 (윈도우 크기게 맞게 설정)
function setCanvasSize() {
    //캔버스의 현재 길이 설정
    canvas.setAttribute("width", document.getElementById("canvas").clientWidth);
    //캔버스의 현재 높이 설정
    canvas.setAttribute("height", document.getElementById("canvas").clientHeight);
}

// 시작 메소드
function runGame() {
    drawAll(); // canvas 위에 이미지를 그려주는 메소드
}

// canvas 위에 이미지를 그려주는 메소드
function drawAll() {
    // canvas의 왼쪽 상단 모서리에서 시작하여 canvas의 전체 영역을 지운다.
    // 새로 받아온 캐릭터 정보들로 다시 그리기 위하여 먼저 캔버스 위에 그려져있는 캐릭터들을 모두 지운다.
    context.clearRect(0, 0, canvas.width, canvas.height);
    // 배경 그리기
    context.drawImage(imgBg, 0, 0, canvas.width, canvas.height);
    for ( character in canvasCharacters ) {
        // 해당 캐릭터의 좌표 정보 배열 가져오기
        coordinate = canvasCharacters[character];
        // 좌표 정보 배열에서 각 좌표를 가져와서 캐릭터 그리기
        imgChar.src = "/imagePath/" + coordinate[0]; // 캐릭터 이미지 주소
        context.drawImage(imgChar, coordinate[1]-w, coordinate[2]-h, w*2, h*2);
        context.font = "bold 15px Arial";
        context.fillStyle = "white";
        context.textAlign = "center";
        context.fillText(character, coordinate[1], coordinate[2] - 25);
    }
}

// canvas 위에 이미지를 그려주는 메소드
function drawChar() {
    // canvas의 왼쪽 상단 모서리에서 시작하여 canvas의 전체 영역을 지운다.
    // 새로 받아온 캐릭터 정보들로 다시 그리기 위하여 먼저 캔버스 위에 그려져있는 캐릭터들을 모두 지운다.
    context.clearRect(0, 0, canvas.width, canvas.height);
    for ( character in canvasCharacters ) {
        // 해당 캐릭터의 좌표 정보 배열 가져오기
        coordinate = canvasCharacters[character];
        // 좌표 정보 배열에서 각 좌표를 가져와서 캐릭터 그리기
        imgChar.src = "/imagePath/" + coordinate[0]; // 캐릭터 이미지 주소
        context.drawImage(imgChar, coordinate[1]-w, coordinate[2]-h, w*2, h*2);
    }
    cancelAnimationFrame(runGame); // 실행되고있는 AnimationFrame 제거
    requestAnimationFrame(runGame); // AnimationFrame 실행
}

// 키가 눌렸을때 실행되는 메소드 - 이동 상태
function keyDown() {
    keycode = event.keyCode; // 키보드 키코드
    // 키코드에 따른 이동 분기 결정
    switch(keycode) {
        // 왼쪽으로 이동
        case 37:
            // send(path, header, message)로 채팅 메시지를 전송한다. (왼쪽 키가 눌릴 경우 여기서 이동 메시지를 전송한다.)
            // JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
            // 여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
            canvasStomp.send('/pub/meta/studyroom/canvas/move', {}, JSON.stringify({type: "left", metaIdx: canvasMetaIdx, writer: canvasNickname, character: canvasProfileImage, canvasLeft: canvasLeft, canvasTop: canvasTop, canvasRight: canvasRight, canvasBottom: canvasBottom}));
            break;
        // 위로 이동
        case 38:
            // send(path, header, message)로 채팅 메시지를 전송한다. (위쪽 키가 눌릴 경우 여기서 이동 메시지를 전송한다.)
            // JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
            // 여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
            canvasStomp.send('/pub/meta/studyroom/canvas/move', {}, JSON.stringify({type: "top", metaIdx: canvasMetaIdx, writer: canvasNickname, character: canvasProfileImage, canvasLeft: canvasLeft, canvasTop: canvasTop, canvasRight: canvasRight, canvasBottom: canvasBottom}));
            break;
        // 오른쪽으로 이동
        case 39:
            // send(path, header, message)로 채팅 메시지를 전송한다. (오른쪽 키가 눌릴 경우 여기서 이동 메시지를 전송한다.)
            // JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
            // 여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
            canvasStomp.send('/pub/meta/studyroom/canvas/move', {}, JSON.stringify({type: "right", metaIdx: canvasMetaIdx, writer: canvasNickname, character: canvasProfileImage, canvasLeft: canvasLeft, canvasTop: canvasTop, canvasRight: canvasRight, canvasBottom: canvasBottom}));
            break;
        // 아래로 이동
        case 40:
            // send(path, header, message)로 채팅 메시지를 전송한다. (아래쪽 키가 눌릴 경우 여기서 이동 메시지를 전송한다.)
            // JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
            // 여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
            canvasStomp.send('/pub/meta/studyroom/canvas/move', {}, JSON.stringify({type: "bottom", metaIdx: canvasMetaIdx, writer: canvasNickname, character: canvasProfileImage, canvasLeft: canvasLeft, canvasTop: canvasTop, canvasRight: canvasRight, canvasBottom: canvasBottom}));
            break;
    }
}

// 눌린 키가 떼졌을때 실행되는 메소드 - 멈춤 상태
function keyUp() {
    var keycode = event.keyCode; // 키보드 키코드
    // 키코드에 따른 멈춤 분기 결정
    switch(keycode) {
        // x축 멈춤
        case 37:
        case 39:
            dx = 0;
            break;
        // y축 멈춤
        case 38:
        case 40:
            dy = 0;
            break;
    }
}