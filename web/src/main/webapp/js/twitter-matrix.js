$(document).ready(function () {
    var CELL_HEIGHT = 10;
    var CELL_WIDTH = 10;
    var BCOLOR = '#000';
    var NEW_CHAR_COLOR = '#0F0';
    var FONT = '10pt Georgia';
    var WIDTH = canv.width = canvover.width = window.innerWidth;
    var HEIGHT = canv.height = canvover.height = window.innerHeight;
    var GRIDN = Math.floor((HEIGHT - CELL_HEIGHT) / CELL_HEIGHT);
    var GRIDM = Math.floor((WIDTH - CELL_WIDTH) / CELL_WIDTH) + 1;

    // Container of matrix cells with references to currently displayed tweets
    var grid = [];
    for (i = CELL_HEIGHT; i <= HEIGHT - CELL_HEIGHT; i += CELL_HEIGHT) {
        var row = [];
        for (h = 0; h <= WIDTH - CELL_WIDTH; h += CELL_WIDTH) {
            row.push({
                x: h,
                y: i,
                tweetKey: null
            })
        }
        grid.push(row);
    }

    // Currently displayed tweets container
    var tweets = new Map();

    // Tweet key that is currently hovered over
    var hoverTweetKey = null;

    var rollers = [];
    for (i = 0; i < GRIDN; i++) {
        rollers.push({
            tweetQueue: [],
            curTweetPos: 0,
            cellInd: 0
        })
    }

    var ctxover = canvover.getContext('2d');
    var ctx = canv.getContext('2d');
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
                var topTweet = roller.tweetQueue[0];
                var topTweetText = topTweet.tweetText;
                var topTweetKey = topTweet.tweetUrl;
                ctx.fillStyle = BCOLOR;
                ctx.fillRect(cell.x, cell.y - CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
                ctx.fillStyle = NEW_CHAR_COLOR;
                ctx.fillText(topTweetText[roller.curTweetPos], cell.x, cell.y);

                if (cell.tweetKey) {
                    tweetInfo = tweets.get(cell.tweetKey);
                    if (tweetInfo.count <= 1) {
                        tweets.delete(cell.tweetKey);
                    } else {
                        tweetInfo.count--;
                    }
                }
                cell.tweetKey = topTweetKey;
                if (tweets.has(topTweetKey)) {
                    tweetInfo = tweets.get(topTweetKey);
                    tweetInfo.count++;
                    tweetInfo.textOffset = roller.curTweetPos;
                } else {
                    tweets.set(topTweetKey, {
                        stGridI: i,
                        stGridH: roller.cellInd,
                        url: topTweet.tweetUrl,
                        text: topTweetText,
                        textOffset: roller.curTweetPos,
                        count: 1
                    });
                }

                roller.curTweetPos++;
                if (roller.curTweetPos >= topTweetText.length) {
                    roller.curTweetPos = 0;
                    roller.tweetQueue.shift();
                }
                roller.cellInd++;
                if (roller.cellInd >= GRIDM) {
                    roller.cellInd = 0;
                }
            }
        }

        ctxover.clearRect(0, 0, WIDTH, HEIGHT);
        ctxover.font = FONT;
        if (hoverTweetKey) {
            tweetInfo = tweets.get(hoverTweetKey);
            gi = tweetInfo.stGridI;
            gh = tweetInfo.stGridH;
            for (i = 0; i <= tweetInfo.textOffset; i++) {
                cell = grid[gi][gh];
                ctxover.clearRect(cell.x, cell.y - CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
                ctxover.fillStyle = '#FFF';
                ctxover.fillText(tweetInfo.text[i], cell.x, cell.y);
                gh++;
                if (gh >= GRIDM) {
                    gh = 0;
                }
            }
        }

    };
    RunMatrix();
    function RunMatrix() {
        if (typeof Game_Interval != "undefined") clearInterval(Game_Interval);
        Game_Interval = setInterval(draw, 33);
    }

    canvover.addEventListener('click', function (e) {
        tweetKey = mouseEventToGridCell(this, e).tweetKey;
        if (tweetKey) {
            window.open(tweets.get(tweetKey).url);
        }
    });

    canvover.addEventListener('mousemove', function(e) {
        hoverTweetKey = mouseEventToGridCell(this, e).tweetKey;
    });

    function mouseEventToGridCell(ref, e) {
        var clickedX = e.pageX - ref.offsetLeft;
        var clickedY = e.pageY - ref.offsetTop;
        var gridI = Math.floor(clickedY / CELL_HEIGHT);
        var gridH = Math.floor(clickedX / CELL_WIDTH);
        return grid[gridI][gridH];
    }

    url = "ws://" +  location.hostname + ":61614/stomp";
    client = Stomp.client(url);

    client.connect({}, function() {
        client.subscribe("/topic/twitter.tweet",
            function( message ) {
                var row = Math.floor(Math.random() * rollers.length);
                rollers[row].tweetQueue.push(JSON.parse(message.body));
            },
            { priority: 9 }
        );
    });
});