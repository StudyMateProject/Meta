// 윈도우 시작시 가장 먼저 실행되는 메소드
window.onload = function(e) {
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
    // 실행되고있는 AnimationFrame 삭제 - 이 작업을 먼저 안하면 실행중인 AnimationFrame이 제거되지 않고 계속 쌓이게 된다.
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
    moveAll(); // 캐릭터에 움직임을 주는 메소드
    drawAll(); // canvas 위에 이미지를 그려주는 메소드
}

// 캐릭터에 움직임을 주는 메소드
function moveAll() {
    x += dx;
    y += dy;
}

// canvas 위에 이미지를 그려주는 메소드
function drawAll() {
    // 배경 그리기
    context.drawImage(imgBg, 0, 0, canvas.width, canvas.height);
    // 캐릭터 동작에 따라 바뀌게 만든다.
    if (imgC == imgChar) { // 그림 이름이 캐릭터 1번 동작일 경우
        // 캐릭터 2번 동작을 그려준다.
        context.drawImage(imgChar2, x-w, y-h, w*2, h*2);
        // 그림 이름을 캐릭터 2번 동작으로 바꿔준다.
        imgC = imgChar2;
    } else { // 그림 이름이 캐릭터 2번 동작일 경우
        // 캐릭터 1번 동작을 그려준다.
        context.drawImage(imgChar, x-w, y-h, w*2, h*2);
        // 그림 이름을 캐릭터 1번 동작으로 바꿔준다.
        imgC = imgChar;
    }
}

// 키가 눌렸을때 실행되는 메소드 - 움직임 상태
function keyDown() {
    keycode = event.keyCode; // 키보드 키코드
    switch(keycode) {
        // 왼쪽으로 이동
        case 37:
            cancelAnimationFrame(runGame); // 실행되고있는 AnimationFrame 삭제
            requestAnimationFrame(runGame); // AnimationFrame 실행
            if ( x < canvasLeft ) { // 왼쪽 벽이 나오면 멈춘다.
                dx = 0;
                if ( y < canvasTop ) { // 위쪽 벽이 나오면 멈춘다.
                    dy = 0;
                    break;
                }
                if ( y > canvasBottom ) { // 아래쪽 벽이 나오면 멈춘다.
                    dy = 0;
                    break;
                }
                break;
            } else { // 왼쪽 벽이 나오기 전까지 움직인다.
                dx = -5;
                if ( y < canvasTop ) { // 위쪽 벽이 나오면 멈춘다.
                    dy = 0;
                    break;
                }
                if ( y > canvasBottom ) { // 아래쪽 벽이 나오면 멈춘다.
                    dy = 0;
                    break;
                }
                break;
            }
        // 위로 이동
        case 38:
            cancelAnimationFrame(runGame); // 실행되고있는 AnimationFrame 삭제
            requestAnimationFrame(runGame); // AnimationFrame 실행
            if ( y < canvasTop ) { // 위쪽 벽이 나오면 멈춘다.
                dy = 0;
                if ( x < canvasLeft ) { // 왼쪽 벽이 나오면 멈춘다.
                    dx = 0;
                    break;
                }
                if ( x > canvasRight ) { // 오른쪽 벽이 나오면 멈춘다.
                    dx = 0;
                    break;
                }
                break;
            } else { // 위쪽 벽이 나오기 전까지 움직인다.
                dy = -5;
                if ( x < canvasLeft ) { // 왼쪽 벽이 나오면 멈춘다.
                    dx = 0;
                    break;
                }
                if ( x > canvasRight ) { // 오른쪽 벽이 나오면 멈춘다.
                    dx = 0;
                    break;
                }
                break;
            }
        // 오른쪽으로 이동
        case 39:
            cancelAnimationFrame(runGame); // 실행되고있는 AnimationFrame 삭제
            requestAnimationFrame(runGame); // AnimationFrame 실행
            if ( x > canvasRight ) { // 오른쪽 벽이 나오면 멈춘다.
                dx = 0;
                if ( y < canvasTop ) { // 위쪽 벽이 나오면 멈춘다.
                    dy = 0;
                    break;
                }
                if ( y > canvasBottom ) { // 아래쪽 벽이 나오면 멈춘다.
                    dy = 0;
                    break;
                }
                break;
            } else { // 오른쪽 벽이 나오기 전까지 움직인다.
                dx = 5;
                if ( y < canvasTop ) { // 위쪽 벽이 나오면 멈춘다.
                    dy = 0;
                    break;
                }
                if ( y > canvasBottom ) { // 아래쪽 벽이 나오면 멈춘다.
                    dy = 0;
                    break;
                }
                break;
            }
        // 아래로 이동
        case 40:
            cancelAnimationFrame(runGame); // 실행되고있는 AnimationFrame 삭제
            requestAnimationFrame(runGame); // AnimationFrame 실행
            if ( y > canvasBottom ) { // 아래쪽 벽이 나오면 멈춘다.
                dy = 0;
                if ( x < canvasLeft ) { // 왼쪽 벽이 나오면 멈춘다.
                    dx = 0;
                    break;
                }
                if ( x > canvasRight ) { // 오른쪽 벽이 나오면 멈춘다.
                    dx = 0;
                    break;
                }
                break;
            } else { // 아래쪽 벽이 나오기 전까지 움직인다.
                dy = 5;
                if ( x < canvasLeft ) { // 왼쪽 벽이 나오면 멈춘다.
                    dx = 0;
                    break;
                }
                if ( x > canvasRight ) { // 오른쪽 벽이 나오면 멈춘다.
                    dx = 0;
                    break;
                }
                break;
            }
    }
}

// 눌린 키가 떼졌을때 실행되는 메소드 - 멈춤 상태
function keyUp() {
    keycode = event.keyCode; // 키보드 키코드
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