$(document).ready(function () {
    var CELL_HEIGHT = 10;
    var CELL_WIDTH = 10;
    var BCOLOR = '#000';
    var NEW_CHAR_COLOR = '#0F0';
    var FONT = '10pt Georgia';
    var WIDTH = q.width = window.innerWidth;
    var HEIGHT = q.height = window.innerHeight;
    var GRIDN = Math.floor((HEIGHT - CELL_HEIGHT) / CELL_HEIGHT);
    var GRIDM = Math.floor((WIDTH - CELL_WIDTH) / CELL_WIDTH) + 1;

    // Container of matrix cells with references to currently displayed tweet
    var grid = [];
    for (i = CELL_HEIGHT; i <= HEIGHT - CELL_HEIGHT; i += CELL_HEIGHT) {
        var row = [];
        for (h = 0; h <= WIDTH - CELL_WIDTH; h += CELL_WIDTH) {
            row.push({
                x: h,
                y: i,
                tweetKey: null,
                tweetOffset: null
            })
        }
        grid.push(row);
    }

    // Currently displayed tweets container
    var tweets = new Map();

    var rollers = [];
    for (i = 0; i < GRIDN; i++) {
        rollers.push({
            tweetQueue: [],
            curTweetPos: 0,
            cellInd: 0
        })
    }

    var ctx = q.getContext('2d');
    // black screen
    ctx.fillStyle = BCOLOR;
    ctx.fillRect(0, 0, WIDTH, HEIGHT);

    var draw = function () {
        ctx.fillStyle = 'rgba(0,0,0,.01)';
        ctx.fillRect(0, 0, WIDTH, HEIGHT);
        ctx.fillStyle = NEW_CHAR_COLOR;
        ctx.font = FONT;

        for (i = 0; i < GRIDN; i++) {
            roller = rollers[i];
            if (roller.tweetQueue.length > 0) {
                var cell = grid[i][roller.cellInd];
                var topMsg = roller.tweetQueue[0];
                ctx.fillStyle = BCOLOR;
                ctx.fillRect(cell.x, cell.y - CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
                ctx.fillStyle = NEW_CHAR_COLOR;
                ctx.fillText(topMsg[roller.curTweetPos], cell.x, cell.y);

                if (cell.tweetKey) {
                    value = tweets.get(cell.tweetKey);
                    if (value <= 1) {
                        tweets.delete(cell.tweetKey);
                    } else {
                        tweets.set(cell.tweetKey, value - 1);
                    }
                }
                cell.tweetKey = topMsg;
                cell.tweetOffset = roller.curTweetPos;
                if (tweets.has(topMsg)) {
                    tweets.set(topMsg, tweets.get(topMsg) + 1)
                } else {
                    tweets.set(topMsg, 1);
                }

                roller.curTweetPos++;
                if (roller.curTweetPos >= topMsg.length) {
                    roller.curTweetPos = 0;
                    roller.tweetQueue.shift();
                }
                roller.cellInd++;
                if (roller.cellInd >= GRIDM) {
                    roller.cellInd = 0;
                }
            }
        }
    };
    RunMatrix();
    function RunMatrix() {
        if (typeof Game_Interval != "undefined") clearInterval(Game_Interval);
        Game_Interval = setInterval(draw, 33);
    }

    q.addEventListener('click', function (e) {
        var clickedX = e.pageX - this.offsetLeft;
        var clickedY = e.pageY - this.offsetTop;
        var gridI = Math.floor(clickedY / CELL_HEIGHT);
        var gridH = Math.floor(clickedX / CELL_WIDTH);
        var cell = grid[gridI][gridH];
        copyTextToClipboard(cell.tweetKey);
    });

    function copyTextToClipboard(text) {
        var textArea = document.createElement("textarea");
        textArea.style.position = 'fixed';
        textArea.style.top = 0;
        textArea.style.left = 0;
        textArea.style.width = '2em';
        textArea.style.height = '2em';
        textArea.style.padding = 0;
        textArea.style.border = 'none';
        textArea.style.outline = 'none';
        textArea.style.boxShadow = 'none';
        textArea.style.background = 'transparent';

        textArea.value = text;

        document.body.appendChild(textArea);

        textArea.select();

        reportFailure = function() {
            console.log('Oops, unable to copy, text=' + text);
        };
        try {
            var successful = document.execCommand('copy');
        } catch (err) {
            reportFailure();
        }
        if (!successful) {
            reportFailure();
        }

        document.body.removeChild(textArea);
    }

    url = "ws://" +  location.hostname + ":61614/stomp";
    client = Stomp.client(url);

    client.connect({}, function() {
        client.subscribe("/topic/twitter.tweet",
            function( message ) {
                var row = Math.floor(Math.random() * rollers.length);
                rollers[row].tweetQueue.push(message.body);
            },
            { priority: 9 }
        );
    });
});