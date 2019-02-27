-- phpMyAdmin SQL Dump
-- version 4.8.3
-- https://www.phpmyadmin.net/
--
-- Хост: 127.0.0.1
-- Время создания: Фев 26 2019 г., 19:13
-- Версия сервера: 10.1.37-MariaDB
-- Версия PHP: 7.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- База данных: `kinohall`
--

DELIMITER $$
--
-- Процедуры
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `bookCancel30minutes` ()  BEGIN
  update books b
    join seances s
    on b.seanceID = s.seanceID
  set recordStatus = "deleted"
  where  to_seconds(s.startTime) - to_seconds(now())<1800
    and to_seconds(s.startTime)- to_seconds(now())>0;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `seanceArchiving` ()  BEGIN
  update seances set expirationStatus = "deleted"
  where now()>date_add(startTime,interval duration minute );
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Структура таблицы `books`
--

CREATE TABLE `books` (
  `bookID` int(11) NOT NULL,
  `seanceID` int(11) NOT NULL,
  `row` int(11) NOT NULL,
  `seat` int(11) NOT NULL,
  `bookCode` bigint(25) DEFAULT NULL,
  `status` varchar(50),
  `recordStatus` varchar(50) NOT NULL,
  `chatID` bigint(25) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Дамп данных таблицы `books`
--

INSERT INTO `books` (`bookID`, `seanceID`, `row`, `seat`, `bookCode`, `status`, `recordStatus`, `chatID`) VALUES
(181, 15, 1, 1, 833239, 'booked', 'deleted', 797147185),
(182, 5, 2, 2, 515766, 'booked', 'deleted', 797147185),
(183, 5, 2, 3, 515766, 'booked', 'deleted', 797147185),
(184, 5, 2, 4, 515766, 'booked', 'deleted', 797147185),
(185, 5, 2, 5, 515766, 'booked', 'deleted', 797147185),
(186, 5, 2, 6, 515766, 'booked', 'deleted', 797147185),
(187, 5, 2, 7, 515766, 'booked', 'deleted', 797147185),
(188, 18, 1, 2, 56907, 'booked', 'deleted', 797147185),
(189, 27, 2, 2, 534163, 'booked', 'deleted', 797147185),
(190, 3, 1, 2, 970196, 'booked', 'deleted', 797147185),
(191, 3, 1, 3, 970196, 'booked', 'deleted', 797147185),
(192, 3, 1, 4, 970196, 'booked', 'deleted', 797147185),
(193, 9, 3, 3, 549900, 'booked', 'deleted', 797147185),
(194, 31, 3, 1, 261499, 'booked', 'deleted', 797147185),
(195, 31, 3, 2, 261499, 'booked', 'deleted', 797147185),
(196, 31, 3, 3, 261499, 'booked', 'deleted', 797147185),
(197, 31, 3, 4, 261499, 'booked', 'deleted', 797147185),
(198, 31, 3, 5, 261499, 'booked', 'deleted', 797147185),
(199, 31, 3, 6, 261499, 'booked', 'deleted', 797147185),
(200, 31, 3, 7, 261499, 'booked', 'deleted', 797147185),
(201, 3, 1, 2, 290345, 'booked', 'deleted', 797147185),
(202, 13, 2, 1, 135848, 'booked', 'deleted', 797147185),
(203, 37, 1, 1, 795718, 'booked', 'deleted', 797147185),
(204, 4, 2, 1, 337242, 'booked', 'deleted', 797147185),
(205, 52, 3, 2, 737153, 'booked', 'deleted', 797147185),
(206, 32, 2, 1, 584017, 'booked', 'deleted', 797147185),
(207, 27, 2, 1, 846213, 'booked', 'deleted', 797147185),
(208, 35, 2, 3, 424572, 'booked', 'deleted', 797147185),
(209, 35, 2, 4, 424572, 'booked', 'deleted', 797147185),
(210, 35, 5, 3, 288781, 'booked', 'deleted', 797147185),
(211, 43, 2, 1, 68048, 'booked', 'deleted', 797147185),
(212, 5, 4, 5, 703285, 'booked', 'active', 513423414),
(213, 18, 3, 2, 512196, 'booked', 'deleted', 797147185),
(214, 44, 1, 3, 194365, 'booked', 'deleted', 797147185),
(215, 22, 2, 2, 955149, 'booked', 'deleted', 797147185),
(216, 4, 2, 2, 975659, 'booked', 'deleted', 797147185),
(217, 19, 2, 2, 794834, 'booked', 'deleted', 797147185),
(218, 9, 1, 1, 496546, 'booked', 'deleted', 797147185),
(219, 30, 7, 5, 31433, 'booked', 'active', 513423414),
(220, 30, 7, 6, 31433, 'booked', 'active', 513423414),
(221, 30, 7, 7, 31433, 'booked', 'active', 513423414),
(222, 30, 7, 8, 31433, 'booked', 'active', 513423414),
(223, 30, 7, 9, 31433, 'booked', 'active', 513423414),
(224, 45, 1, 4, 104899, 'booked', 'deleted', 642578902),
(225, 4, 2, 1, 290212, 'booked', 'deleted', 797147185),
(226, 32, 4, 4, 663103, 'booked', 'active', 228576970),
(227, 4, 4, 1, 709703, 'booked', 'active', 621607482),
(228, 25, 2, 2, 56980, 'booked', 'deleted', 797147185),
(229, 25, 2, 3, 56980, 'booked', 'deleted', 797147185),
(230, 25, 2, 4, 56980, 'booked', 'deleted', 797147185),
(231, 9, 3, 4, 710800, 'booked', 'deleted', 611298335),
(232, 21, 3, 3, 446779, 'booked', 'active', 797147185),
(233, 21, 3, 4, 446779, 'booked', 'active', 797147185),
(234, 21, 3, 5, 446779, 'booked', 'active', 797147185),
(235, 21, 3, 6, 446779, 'booked', 'active', 797147185),
(236, 21, 3, 7, 446779, 'booked', 'active', 797147185),
(237, 21, 3, 8, 446779, 'booked', 'active', 797147185),
(238, 21, 3, 9, 446779, 'booked', 'active', 797147185),
(239, 5, 7, 2, 252053, 'booked', 'active', 797147185),
(240, 5, 7, 3, 252053, 'booked', 'active', 797147185),
(241, 5, 7, 4, 252053, 'booked', 'active', 797147185),
(242, 5, 7, 5, 252053, 'booked', 'active', 797147185),
(243, 5, 7, 6, 252053, 'booked', 'active', 797147185),
(244, 5, 7, 7, 252053, 'booked', 'active', 797147185),
(245, 5, 7, 8, 252053, 'booked', 'active', 797147185),
(246, 5, 7, 9, 252053, 'booked', 'active', 797147185),
(247, 5, 5, 4, 145075, 'booked', 'active', 797147185),
(248, 5, 5, 5, 145075, 'booked', 'active', 797147185),
(249, 5, 5, 6, 145075, 'booked', 'active', 797147185),
(250, 5, 5, 7, 145075, 'booked', 'active', 797147185),
(251, 5, 5, 8, 145075, 'booked', 'active', 797147185),
(252, 5, 5, 9, 145075, 'booked', 'active', 797147185),
(253, 1, 1, 2, 220726, 'booked', 'deleted', 797147185),
(254, 1, 4, 4, 694962, 'booked', 'deleted', 797147185),
(255, 1, 6, 6, 194888, 'booked', 'deleted', 797147185),
(256, 2, 1, 1, 755685, 'booked', 'deleted', 797147185),
(257, 1, 1, 1, 625139, 'booked', 'active', 797147185),
(258, 15, 2, 2, 715582, 'booked', 'deleted', 797147185),
(259, 15, 4, 4, 774364, 'booked', 'deleted', 797147185),
(260, 39, 2, 2, 806672, 'booked', 'active', 797147185),
(261, 5, 1, 1, 120164, 'booked', 'deleted', 797147185),
(262, 5, 1, 2, 120164, 'booked', 'deleted', 797147185),
(263, 5, 1, 3, 120164, 'booked', 'deleted', 797147185),
(264, 5, 1, 4, 120164, 'booked', 'deleted', 797147185),
(265, 5, 1, 5, 120164, 'booked', 'deleted', 797147185),
(266, 5, 1, 6, 120164, 'booked', 'deleted', 797147185),
(267, 5, 1, 7, 120164, 'booked', 'deleted', 797147185),
(268, 25, 1, 1, 585536, 'booked', 'active', 797147185),
(269, 19, 10, 1, 805772, 'booked', 'active', 452563403),
(270, 19, 10, 2, 805772, 'booked', 'active', 452563403),
(271, 19, 10, 3, 805772, 'booked', 'active', 452563403),
(272, 19, 10, 4, 805772, 'booked', 'active', 452563403),
(273, 19, 10, 5, 805772, 'booked', 'active', 452563403),
(274, 19, 10, 6, 805772, 'booked', 'active', 452563403),
(275, 19, 10, 7, 805772, 'booked', 'active', 452563403),
(276, 19, 10, 8, 805772, 'booked', 'active', 452563403),
(277, 19, 10, 9, 805772, 'booked', 'active', 452563403),
(278, 19, 10, 10, 805772, 'booked', 'active', 452563403);

-- --------------------------------------------------------

--
-- Структура таблицы `hallinfo`
--

CREATE TABLE `hallinfo` (
  `hallID` int(11) NOT NULL,
  `rows` int(11) NOT NULL,
  `seats` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Дамп данных таблицы `hallinfo`
--

INSERT INTO `hallinfo` (`hallID`, `rows`, `seats`) VALUES
(1, 20, 20),
(2, 5, 5),
(3, 14, 35),
(4, 10, 20);

-- --------------------------------------------------------

--
-- Структура таблицы `halls`
--

CREATE TABLE `halls` (
  `hallID` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Дамп данных таблицы `halls`
--

INSERT INTO `halls` (`hallID`) VALUES
(1),
(2),
(3),
(4);

-- --------------------------------------------------------

--
-- Структура таблицы `movieinfo`
--

CREATE TABLE `movieinfo` (
  `movieID` int(11) NOT NULL,
  `productionYear` int(11) DEFAULT NULL,
  `director` varchar(100) DEFAULT NULL,
  `genre` varchar(100) DEFAULT NULL,
  `actors` varchar(255) DEFAULT NULL,
  `ageRestriction` int(11) DEFAULT NULL,
  `ranking` double DEFAULT '1',
  `pathToPoster` varchar(100) DEFAULT 'src/main/resources/posters'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Дамп данных таблицы `movieinfo`
--

INSERT INTO `movieinfo` (`movieID`, `productionYear`, `director`, `genre`, `actors`, `ageRestriction`, `ranking`, `pathToPoster`) VALUES
(2, 2000, 'Ridley Scott', 'Historic, Drama', ' Russell Crowe', 18, 8.5, 'src/main/resources/posters'),
(11, 1994, 'Luc Besson', 'Crime,Drama', 'Jean Reno, Gary Oldman, Natalie Portman', 18, 8.6, 'src/main/resources/posters'),
(13, 2017, 'Patrick Hughes', 'Action, Comedy, Thriller', 'Ryan Reynolds', 18, 6.9, 'src/main/resources/posters'),
(14, 2007, 'Xavier Gens', 'Action, Crime, Drama', 'Timothy Olyphant', 18, 6.3, 'src/main/resources/posters'),
(15, 2015, 'Colin Trevorrow', 'Adventure, Sci-fi, Thriller', 'Chris Pratt, Bryce Dallas Howard, Ty Simpkins', 18, 7, 'src/main/resources/posters'),
(17, 1993, 'Steven Spielberg', 'Adventure, Sci-fi, Thriller', 'Sam Neill', 18, 8.1, 'src/main/resources/posters'),
(18, 1995, 'Michael Mann', 'Crime, Drama, Thriller', 'Al Pacino, Robert De Niro, Val Kilmer', 18, 6.2, 'src/main/resources/posters'),
(19, 1972, 'Francis Ford Coppola', 'Crime, Drama', 'Marlon Brando, Al Pacino, James Caan', 18, 9.2, 'src/main/resources/posters'),
(20, 1984, 'James Cameron', 'Action, Sci-Fi', 'Arnold Schwarzenegger, Linda Hamilton, Michael Biehn', 18, 9, 'src/main/resources/posters'),
(21, 1987, 'John McTiernan', 'Action, Adventure, Sci-Fi', 'Arnold Schwarzenegger, Carl Weathers, Kevin Peter Hall', 18, 7.6, 'src/main/resources/posters');

-- --------------------------------------------------------

--
-- Структура таблицы `movies`
--

CREATE TABLE `movies` (
  `movieID` int(11) NOT NULL,
  `movie` varchar(200) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Дамп данных таблицы `movies`
--

INSERT INTO `movies` (`movieID`, `movie`) VALUES
(2, 'Gladiator'),
(19, 'Godfather'),
(18, 'Heat'),
(14, 'Hitman'),
(17, 'Jurassic Park'),
(15, 'Jurassic World'),
(11, 'Leon'),
(21, 'Predator'),
(20, 'Terminator'),
(13, 'The Hitman\'s Bodyguard');

-- --------------------------------------------------------

--
-- Структура таблицы `placesstatus`
--

CREATE TABLE `placesstatus` (
  `statusID` int(11) NOT NULL,
  `status` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Дамп данных таблицы `placesstatus`
--

INSERT INTO `placesstatus` (`statusID`, `status`) VALUES
(2, 'booked'),
(3, 'bought'),
(1, 'free');

-- --------------------------------------------------------

--
-- Структура таблицы `seances`
--

CREATE TABLE `seances` (
  `seanceID` int(11) NOT NULL,
  `hallID` int(11) NOT NULL,
  `movieID` int(11) NOT NULL,
  `startTime` datetime NOT NULL,
  `duration` int(11) NOT NULL,
  `expirationStatus` varchar(50) NOT NULL DEFAULT 'active',
  `price` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Дамп данных таблицы `seances`
--

INSERT INTO `seances` (`seanceID`, `hallID`, `movieID`, `startTime`, `duration`, `expirationStatus`, `price`) VALUES
(1, 1, 13, '2019-02-26 01:20:00', 120, 'active', 1100),
(2, 1, 2, '2019-02-26 01:22:00', 120, 'active', 1000),
(3, 1, 21, '2019-02-26 16:00:00', 150, 'active', 2000),
(4, 1, 18, '2019-02-26 19:00:00', 120, 'active', 3000),
(5, 1, 14, '2019-02-26 23:28:00', 120, 'active', 2500),
(6, 2, 2, '2019-02-26 11:00:00', 120, 'active', 2000),
(7, 2, 21, '2019-02-26 14:00:00', 120, 'active', 2200),
(8, 2, 20, '2019-02-26 16:00:00', 120, 'active', 3200),
(9, 2, 17, '2019-02-26 18:00:00', 170, 'active', 1000),
(10, 3, 2, '2019-02-26 11:00:00', 120, 'active', 2000),
(11, 3, 17, '2019-02-26 13:00:00', 120, 'active', 1500),
(12, 3, 11, '2019-02-26 15:00:00', 120, 'active', 2000),
(13, 3, 19, '2019-02-26 18:00:00', 120, 'active', 3000),
(14, 3, 2, '2019-02-26 21:00:00', 120, 'active', 2400),
(15, 4, 21, '2019-02-26 01:50:00', 110, 'active', 1000),
(16, 4, 20, '2019-02-26 12:00:00', 120, 'active', 1400),
(17, 4, 19, '2019-02-26 15:00:00', 120, 'active', 1500),
(18, 4, 2, '2019-02-26 18:00:00', 120, 'active', 2000),
(19, 4, 15, '2019-02-26 22:00:00', 120, 'active', 2000),
(20, 1, 2, '2019-02-27 11:00:00', 120, 'active', 3000),
(21, 1, 21, '2019-02-27 16:00:00', 150, 'active', 4000),
(22, 1, 2, '2019-02-27 19:00:00', 120, 'active', 2000),
(23, 1, 14, '2019-02-27 22:00:00', 120, 'active', 2500),
(24, 2, 19, '2019-02-27 11:00:00', 120, 'active', 2500),
(25, 2, 21, '2019-02-27 14:00:00', 120, 'active', 2500),
(26, 2, 20, '2019-02-27 16:00:00', 120, 'active', 2000),
(27, 2, 20, '2019-02-27 18:00:00', 170, 'active', 5000),
(28, 3, 2, '2019-02-27 11:00:00', 120, 'active', 2000),
(29, 3, 17, '2019-02-27 13:00:00', 120, 'active', 4000),
(30, 3, 11, '2019-02-27 15:00:00', 120, 'active', 3000),
(31, 3, 19, '2019-02-27 18:00:00', 120, 'active', 1000),
(32, 3, 2, '2019-02-27 21:40:00', 120, 'active', 3000),
(33, 4, 21, '2019-02-27 10:00:00', 110, 'active', 4000),
(34, 4, 20, '2019-02-27 12:00:00', 120, 'active', 3000),
(35, 4, 19, '2019-02-27 15:00:00', 120, 'active', 2000),
(36, 4, 2, '2019-02-27 18:10:00', 120, 'active', 2000),
(37, 4, 15, '2019-02-26 22:50:03', 120, 'active', 4000),
(38, 1, 2, '2019-02-28 11:10:00', 100, 'active', 2400),
(39, 1, 21, '2019-02-28 16:20:00', 110, 'active', 2300),
(40, 1, 2, '2019-02-28 19:50:00', 120, 'active', 2000),
(41, 1, 14, '2019-02-28 22:20:00', 110, 'active', 1500),
(42, 2, 19, '2019-02-28 11:20:00', 120, 'active', 3400),
(43, 2, 21, '2019-02-28 14:10:00', 110, 'active', 2400),
(44, 2, 20, '2019-02-28 16:30:00', 100, 'active', 2300),
(45, 2, 20, '2019-02-28 18:10:00', 120, 'active', 2500),
(46, 3, 2, '2019-02-28 11:22:00', 111, 'active', 3000),
(47, 3, 17, '2019-02-28 13:10:00', 120, 'active', 4000),
(48, 3, 17, '2019-02-28 15:10:00', 122, 'active', 2000),
(49, 3, 2, '2019-02-28 18:34:00', 120, 'active', 1000),
(50, 3, 21, '2019-02-28 21:10:00', 110, 'active', 3000),
(51, 4, 19, '2019-02-28 10:10:00', 120, 'active', 4000),
(52, 4, 20, '2019-02-28 12:20:00', 100, 'active', 2000),
(53, 4, 15, '2019-02-28 15:30:00', 100, 'active', 2300),
(54, 4, 21, '2019-02-28 18:10:00', 120, 'active', 2300),
(55, 4, 19, '2019-02-28 22:14:03', 120, 'active', 3400);

-- --------------------------------------------------------

--
-- Структура таблицы `users`
--

CREATE TABLE `users` (
  `email` varchar(100) DEFAULT NULL,
  `firstName` varchar(100) DEFAULT NULL,
  `lastName` varchar(100) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `id` int(11) NOT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Индексы сохранённых таблиц
--

--
-- Индексы таблицы `books`
--
ALTER TABLE `books`
  ADD PRIMARY KEY (`bookID`),
  ADD KEY `books_seances_seanceID_fk` (`seanceID`),
  ADD KEY `books_placesstatus_status_fk` (`status`);

--
-- Индексы таблицы `hallinfo`
--
ALTER TABLE `hallinfo`
  ADD UNIQUE KEY `hallinfo_hallID_uindex` (`hallID`);

--
-- Индексы таблицы `halls`
--
ALTER TABLE `halls`
  ADD PRIMARY KEY (`hallID`);

--
-- Индексы таблицы `movieinfo`
--
ALTER TABLE `movieinfo`
  ADD UNIQUE KEY `movieInfo_movieID_uindex` (`movieID`);

--
-- Индексы таблицы `movies`
--
ALTER TABLE `movies`
  ADD PRIMARY KEY (`movieID`),
  ADD UNIQUE KEY `movies_movie_uindex` (`movie`),
  ADD KEY `movies_movieID_index` (`movieID`);

--
-- Индексы таблицы `placesstatus`
--
ALTER TABLE `placesstatus`
  ADD PRIMARY KEY (`statusID`),
  ADD UNIQUE KEY `placesStatus_status_uindex` (`status`);

--
-- Индексы таблицы `seances`
--
ALTER TABLE `seances`
  ADD PRIMARY KEY (`seanceID`),
  ADD KEY `hall` (`hallID`),
  ADD KEY `seances_movies_movieID_fk` (`movieID`);

--
-- Индексы таблицы `users`
--
ALTER TABLE `users`
  ADD UNIQUE KEY `users_email_uindex` (`email`);

--
-- AUTO_INCREMENT для сохранённых таблиц
--

--
-- AUTO_INCREMENT для таблицы `books`
--
ALTER TABLE `books`
  MODIFY `bookID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=279;

--
-- AUTO_INCREMENT для таблицы `movies`
--
ALTER TABLE `movies`
  MODIFY `movieID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT для таблицы `placesstatus`
--
ALTER TABLE `placesstatus`
  MODIFY `statusID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT для таблицы `seances`
--
ALTER TABLE `seances`
  MODIFY `seanceID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=56;

--
-- Ограничения внешнего ключа сохраненных таблиц
--

--
-- Ограничения внешнего ключа таблицы `books`
--
ALTER TABLE `books`
  ADD CONSTRAINT `books_placesstatus_status_fk` FOREIGN KEY (`status`) REFERENCES `placesstatus` (`status`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `books_seances_seanceID_fk` FOREIGN KEY (`seanceID`) REFERENCES `seances` (`seanceID`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Ограничения внешнего ключа таблицы `hallinfo`
--
ALTER TABLE `hallinfo`
  ADD CONSTRAINT `hallinfo_halls_hallID_fk` FOREIGN KEY (`hallID`) REFERENCES `halls` (`hallID`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Ограничения внешнего ключа таблицы `movieinfo`
--
ALTER TABLE `movieinfo`
  ADD CONSTRAINT `movieInfo_movies_movieID_fk` FOREIGN KEY (`movieID`) REFERENCES `movies` (`movieID`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Ограничения внешнего ключа таблицы `seances`
--
ALTER TABLE `seances`
  ADD CONSTRAINT `seances_ibfk_1` FOREIGN KEY (`hallID`) REFERENCES `halls` (`hallID`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `seances_movies_movieID_fk` FOREIGN KEY (`movieID`) REFERENCES `movies` (`movieID`) ON DELETE CASCADE ON UPDATE CASCADE;

DELIMITER $$
--
-- События
--
CREATE DEFINER=`root`@`localhost` EVENT `myevent` ON SCHEDULE EVERY 1 MINUTE STARTS '2019-02-16 22:39:43' ON COMPLETION NOT PRESERVE ENABLE DO CALL bookCancel30minutes()$$

DELIMITER ;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
