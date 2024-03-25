import { HeartTwoTone, SmileTwoTone } from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { Alert, Card, Typography } from 'antd';

/**
 * 每个单独的卡片，为了复用样式抽成了组件
 */
const { Title, Paragraph } = Typography;

const blockContent = `通过我们的在线代码生成器制作平台，您不管是专业的编程人员，还是普通的使用者，都可以轻松上手。快速生成属于自己的定制化代码，主打一个效率。`;
const BIIntroduce = `我们引以为傲的亮点是 :
   欢迎来到我们的在线代码生成器平台！我们致力于提供一个强大而便捷的工具，帮助开发人员提高效率、减少错误，并加速项目开发过程。
   我们的代码生成器平台具有许多优点。首先，我们提供了丰富的代码模板和功能，让您能够快速生成常用的代码片段和样板代码。无论是创建表单、处理数据、实现算法，还是构建用户界面，我们都为您提供了一系列预定义的代码模板，节省您编写重复代码的时间。
   无论您使用的是哪种编程语言或框架，我们都支持多种技术栈。我们的平台提供了对不同编程语言和框架的支持，使您能够生成适用于各种项目类型的代码。这为您提供了更大的灵活性和可定制性。`;
const Welcome: React.FC = () => {
  return (
    <PageContainer>
      <Card>
        <Alert
          message={'欢迎使用Enaveng的代码生成器平台！'}
          type="success"
          showIcon
          banner
          style={{
            margin: -12,
            marginBottom: 48,
          }}
        />
        <Typography.Title
          level={1}
          style={{
            textAlign: 'center',
          }}
        >
          <SmileTwoTone style={{ color: '#0015ff' }} /> 在线 Code 生成器平台{' '}
          <HeartTwoTone twoToneColor="#eb2f96" />
        </Typography.Title>
        <Paragraph>
          我们的在线代码生成器平台是简化开发，大幅度提高效率的平台。
        </Paragraph>
        <Paragraph style={{ fontWeight: 'bold' }}>
          <pre>{blockContent}</pre>
        </Paragraph>
        <Title level={2}>在线代码生成器 介绍</Title>
        <Paragraph style={{ fontWeight: 'bold' }}>
          <pre>{BIIntroduce}</pre>
        </Paragraph>
        <Title level={2}>在线代码生成器 特点</Title>
        <Paragraph strong>
          1.
          帮助开发人员快速生成代码片段、模板或样板代码，节省了手动编写重复代码的时间和精力。它们提供了预定义的代码模板和常用功能的自动生成，使开发任务更高效。
        </Paragraph>
        <Paragraph strong>
          2.
          可以在各种操作系统和设备上运行，无需安装额外的软件或工具。它们提供直观的用户界面和易于使用的操作，使得即使对编程不太熟悉的人也能方便地生成所需的代码。
        </Paragraph>
        <Paragraph strong>
          3.
          使开发人员可以根据需要生成适用于各种技术栈和项目类型的代码。这提供了更大的灵活性和可定制性。
        </Paragraph>
        <Paragraph strong>
          4.
          通过使用在线代码生成器，开发人员可以避免手动编写代码时可能引入的错误和拼写错误。生成的代码经过验证，并且是经过测试和调试的，因此减少了出错的可能性，加快了调试过程。
        </Paragraph>
        <br />
      </Card>
    </PageContainer>
  );
};

export default Welcome;
